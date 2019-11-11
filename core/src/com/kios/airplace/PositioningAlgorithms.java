package com.kios.airplace;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.kios.airplace.Positioning.*;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

class PositioningAlgorithms {
	String K = "4";
	RssRadioMap rssRadioMap;
	private MagneticRadioMap magneticRadioMap;

	private double[][] rssLocation;
	private double[][] magneticLocation;
	private double[][] fusionEngineLocation;
	private double[][] particleFilterPredictedLocation;

	private ArrayList<Float[][]> rssCoordinates;
	private ArrayList<Float[][]> magneticCoordinates;

	private ArrayList<Double> rssProbabilities;
	private ArrayList<Double> magneticProbabilities;

	private double[][] rssCovarianceMatrix;
	private double[][] magneticCovarianceMatrix;
	private double[][] fusionEngineCovarianceMatrix;

	boolean is_pf_initialized;
	private Vector2 v1;
	private Vector2 v2;
	private Circle circle;
	private Vector2 center;
	boolean[] logicalArray;

	PositioningAlgorithms() {
		try {
			rssRadioMap = new RssRadioMap();
			magneticRadioMap = new MagneticRadioMap();

			Globals.TOTAL_POINTS = rssRadioMap.getRssLocationHashMap().size();
		} catch (Exception e) {
			e.printStackTrace();
		}

		rssLocation = new double[2][1];
		magneticLocation = new double[2][1];

		rssProbabilities = new ArrayList<>();
		magneticProbabilities = new ArrayList<>();

		rssCoordinates = new ArrayList<>();
		magneticCoordinates = new ArrayList<>();

		rssCovarianceMatrix = new double[2][2];
		magneticCovarianceMatrix = new double[2][2];
		fusionEngineCovarianceMatrix = new double[2][2];

		is_pf_initialized = false;
		v1 = new Vector2();
		v2 = new Vector2();
		circle = new Circle();
		center = new Vector2();
		logicalArray = new boolean[Globals.PARTICLES_NUMBER];
	}

	//RSS
	double[][] getRssLocation() {
		LogRecord tempLR;
		int i, j, notFoundCounter = 0;

		ArrayList<String> macAddressList = rssRadioMap.getMacAddressList();
		ArrayList<String> observedRssValues = new ArrayList<>();

		// Read parameter of algorithm
		String NaNValue = readParameter(0);

		// Check which mac addresses of radio map, we are currently listening.
		for (i = 0; i < macAddressList.size(); ++i) {
			for (j = 0; j < Globals.WIFI_LIST.size(); ++j) {
				tempLR = Globals.WIFI_LIST.get(j);

				// MAC Address Matched
				if (macAddressList.get(i).compareTo(tempLR.getBSSID()) == 0) {
					observedRssValues.add(String.valueOf(tempLR.getLevel()));
					break;
				}
			}
			// A MAC Address is missing so we place a small value, NaN value
			if (j == Globals.WIFI_LIST.size()) {
				observedRssValues.add(String.valueOf(NaNValue));
				++notFoundCounter;
			}
		}

		if (notFoundCounter == macAddressList.size())
			return null;

		// Read parameter of algorithm
		String parameter = readParameter(Globals.SELECTED_ALGORITHM);

		if (parameter == null)
			return null;

		switch (Globals.SELECTED_ALGORITHM) {
			case 1:
				rssLocation = KNN_WKNN_Algorithm(observedRssValues, parameter, false);
				break;
			case 2:
				rssLocation = KNN_WKNN_Algorithm(observedRssValues, parameter, true);
				break;
			case 3:
				rssLocation = MAP_MMSE_Algorithm(observedRssValues, parameter, false);
				break;
			case 4:
				rssLocation = MAP_MMSE_Algorithm(observedRssValues, parameter, true);
				break;
		}
		return rssLocation;
	}

	private float calculateEuclideanDistance(ArrayList<String> l1, ArrayList<String> l2) {
		float v1;
		float v2;
		float temp;
		String str;
		float finalResult = 0;

		for (int i = 0; i < l1.size(); ++i) {
			try {
				str = l1.get(i);
				v1 = Float.parseFloat(str.trim());
				str = l2.get(i);
				v2 = Float.parseFloat(str.trim());
			} catch (Exception e) {
				e.printStackTrace();
				return Float.NEGATIVE_INFINITY;
			}

			// do the procedure
			temp = v1 - v2;
			temp *= temp;

			// do the procedure
			finalResult += temp;
		}
		return ((float) Math.sqrt(finalResult));
	}

	double[][] getRssCovarianceMatrix() {
		return (rssLocation != null) ? rssCovarianceMatrix : null;
	}

	private double calculateProbability(ArrayList<String> l1, ArrayList<String> l2, float sGreek) {
		float v1;
		float v2;
		String str;
		double temp;
		double finalResult = 1;

		for (int i = 0; i < l1.size(); ++i) {
			try {
				str = l1.get(i);
				v1 = Float.parseFloat(str.trim());
				str = l2.get(i);
				v2 = Float.parseFloat(str.trim());
			} catch (Exception e) {
				e.printStackTrace();
				return Double.NEGATIVE_INFINITY;
			}

			temp = v1 - v2;
			temp *= temp;
			temp = -temp;
			temp /= (sGreek * sGreek);
			temp = Math.exp(temp);

			//Do not allow zero instead stop on small possibility
			if (finalResult * temp != 0)
				finalResult = finalResult * temp;
		}
		return finalResult;
	}

	private double[][] KNN_WKNN_Algorithm(ArrayList<String> observedRssValues, String parameter, boolean isWeighted) {
		int K;
		float curResult;
		String myLocation;
		ArrayList<String> rssValues;
		ArrayList<LocDistance> locDistanceResultsList = new ArrayList<>();

		try {
			K = Integer.parseInt(parameter);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		rssCoordinates.clear();
		rssProbabilities.clear();
		// Construct a list with locations-distances pairs for currently observed RSS values
		for (String location : rssRadioMap.getRssLocationHashMap().keySet()) {
			rssValues = rssRadioMap.getRssLocationHashMap().get(location);
			curResult = calculateEuclideanDistance(rssValues, observedRssValues);

			if (curResult == Float.NEGATIVE_INFINITY)
				return null;

			locDistanceResultsList.add(0, new LocDistance(curResult, location));
		}

		// Sort locations-distances pairs based on minimum distances
		Collections.sort(locDistanceResultsList, new Comparator<LocDistance>() {
			public int compare(LocDistance gd1, LocDistance gd2) {
				return (Double.compare(gd1.getDistance(), gd2.getDistance()));
			}
		});

		if (!isWeighted) {
			myLocation = calculateAverageKDistanceLocations(locDistanceResultsList, K);
		} else {
			myLocation = calculateWeightedAverageKDistanceLocations(locDistanceResultsList, K, "rss");
		}

		if (myLocation != null) {
			return new double[][]{{Double.parseDouble(myLocation.split(" ")[0])}, {Double.parseDouble(myLocation.split(" ")[1])}};
		}
		return null;
	}

	private double[][] MAP_MMSE_Algorithm(ArrayList<String> observedRssValues, String parameter, boolean isWeighted) {
		float sGreek;
		double curResult;
		String myLocation = null;
		ArrayList<String> rssValues;
		double highestProbability = Double.NEGATIVE_INFINITY;
		ArrayList<LocDistance> locDistanceResultsList = new ArrayList<>();

		try {
			sGreek = Float.parseFloat(parameter);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		rssCoordinates.clear();
		rssProbabilities.clear();
		// Find the location of user with the highest probability
		for (String location : rssRadioMap.getRssLocationHashMap().keySet()) {
			rssValues = rssRadioMap.getRssLocationHashMap().get(location);
			curResult = calculateProbability(rssValues, observedRssValues, sGreek);

			if (!isWeighted) {
				rssProbabilities.add(curResult);
				rssCoordinates.add(new Float[][]{{Float.parseFloat(location.split(" ")[0])}, {Float.parseFloat(location.split(" ")[1])}});
			}

			if (curResult == Double.NEGATIVE_INFINITY)
				return null;
			else if (curResult > highestProbability) {
				highestProbability = curResult;
				myLocation = location;
			}

			if (isWeighted)
				locDistanceResultsList.add(0, new LocDistance(curResult, location));
		}

		if (isWeighted) {
			myLocation = calculateWeightedAverageProbabilityLocations(locDistanceResultsList, "rss");
		} else {
			double sumProbabilities = 0;
			for (Double rssProbability : rssProbabilities) sumProbabilities += rssProbability;

			for (int i = 0; i < rssProbabilities.size(); i++) {
				rssProbabilities.set(i, rssProbabilities.get(i) / sumProbabilities);
			}
		}
		if (myLocation != null) {
			return new double[][]{{Double.parseDouble(myLocation.split(" ")[0])}, {Double.parseDouble(myLocation.split(" ")[1])}};
		}
		return null;
	}

	//Magnetic
	double[][] getMagneticCovarianceMatrix() {
		return (magneticLocation != null) ? magneticCovarianceMatrix : null;
	}

	private double calculateEuclideanDistance(MagneticValues v1, MagneticValues v2) {
		return Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2) + Math.pow(v1.z - v2.z, 2));
	}

	private double calculateProbability(MagneticValues v1, MagneticValues v2, float sGreek) {
		double p1 = 0;
		double p2 = 0;
		double temp;
		double finalResult = 1;

		for (int i = 0; i < 3; ++i) {
			try {
				switch (i % 3) {
					case 0:
						p1 = v1.x;
						p2 = v2.x;
						break;
					case 1:
						p1 = v1.y;
						p2 = v2.y;
						break;
					case 2:
						p1 = v1.z;
						p2 = v2.z;
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Double.NEGATIVE_INFINITY;
			}

			temp = p1 - p2;
			temp *= temp;
			temp = -temp;

			temp /= sGreek * sGreek;
			temp = Math.exp(temp);

			//Do not allow zero instead stop on small possibility
			if (finalResult * temp != 0)
				finalResult = finalResult * temp;
		}
		return finalResult;
	}

	double[][] getMagneticLocation() {
		// Read parameter of algorithm
		String parameter = readParameter(Globals.SELECTED_ALGORITHM);

		MagneticValues observedMagneticValues = new MagneticValues(Globals.MAGNETIC_FIELD.get(0), Globals.MAGNETIC_FIELD.get(1), Globals.MAGNETIC_FIELD.get(2));

		if (parameter == null)
			return null;

		switch (Globals.SELECTED_ALGORITHM) {
			case 1:
				magneticLocation = KNN_WKNN_Algorithm(observedMagneticValues, parameter, false);
				break;
			case 2:
				magneticLocation = KNN_WKNN_Algorithm(observedMagneticValues, parameter, true);
				break;
			case 3:
				magneticLocation = MAP_MMSE_Algorithm(observedMagneticValues, parameter, false);
				break;
			case 4:
				magneticLocation = MAP_MMSE_Algorithm(observedMagneticValues, parameter, true);
				break;
		}
		return magneticLocation;
	}

	private double[][] MAP_MMSE_Algorithm(MagneticValues observedRssValues, String parameter, boolean isWeighted) {
		float sGreek;
		double curResult;
		MagneticValues magneticValues;
		String myLocation = null;
		double highestProbability = Double.NEGATIVE_INFINITY;
		ArrayList<LocDistance> locDistanceResultsList = new ArrayList<>();

		try {
			sGreek = Float.parseFloat(parameter);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		magneticCoordinates.clear();
		magneticProbabilities.clear();
		// Find the location of user with the highest probability
		for (String location : magneticRadioMap.getMagneticLocationHashMap().keySet()) {
			magneticValues = magneticRadioMap.getMagneticLocationHashMap().get(location);
			curResult = calculateProbability(magneticValues, observedRssValues, sGreek);

			if (!isWeighted) {
				magneticProbabilities.add(curResult);
				magneticCoordinates.add(new Float[][]{{Float.parseFloat(location.split(" ")[0])}, {Float.parseFloat(location.split(" ")[1])}});
			}

			if (curResult == Double.NEGATIVE_INFINITY)
				return null;
			else if (curResult > highestProbability) {
				highestProbability = curResult;
				myLocation = location;
			}

			if (isWeighted)
				locDistanceResultsList.add(0, new LocDistance(curResult, location));
		}

		if (isWeighted) {
			myLocation = calculateWeightedAverageProbabilityLocations(locDistanceResultsList, "magnetic");
		} else {
			float sumProbabilities = 0;
			for (Double magneticProbability : magneticProbabilities)
				sumProbabilities += magneticProbability;

			for (int i = 0; i < magneticProbabilities.size(); i++) {
				magneticProbabilities.set(i, magneticProbabilities.get(i) / sumProbabilities);
			}
		}
		if (myLocation != null) {
			return new double[][]{{Double.parseDouble(myLocation.split(" ")[0])}, {Double.parseDouble(myLocation.split(" ")[1])}};
		}
		return null;
	}

	private double[][] KNN_WKNN_Algorithm(MagneticValues observedMagneticValues, String parameter, boolean isWeighted) {
		int K;
		double curResult;
		String myLocation;
		MagneticValues magneticValues;
		ArrayList<LocDistance> locDistanceResultsList = new ArrayList<>();

		try {
			K = Integer.parseInt(parameter);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		magneticCoordinates.clear();
		magneticProbabilities.clear();
		// Construct a list with locations-distances pairs for currently observed magnetic values
		for (String location : magneticRadioMap.getMagneticLocationHashMap().keySet()) {
			magneticValues = magneticRadioMap.getMagneticLocationHashMap().get(location);
			curResult = calculateEuclideanDistance(magneticValues, observedMagneticValues);

			if (curResult == Float.NEGATIVE_INFINITY)
				return null;

			locDistanceResultsList.add(0, new LocDistance(curResult, location));
		}

		// Sort locations-distances pairs based on minimum distances
		Collections.sort(locDistanceResultsList, new Comparator<LocDistance>() {
			public int compare(LocDistance gd1, LocDistance gd2) {
				return (Double.compare(gd1.getDistance(), gd2.getDistance()));
			}
		});

		if (!isWeighted) {
			myLocation = calculateAverageKDistanceLocations(locDistanceResultsList, K);
		} else {
			myLocation = calculateWeightedAverageKDistanceLocations(locDistanceResultsList, K, "magnetic");
		}
		if (myLocation != null) {
			return new double[][]{{Double.parseDouble(myLocation.split(" ")[0])}, {Double.parseDouble(myLocation.split(" ")[1])}};
		}
		return null;
	}

	//Fusion Engine
	double[][] getFusionEngineLocation() {
		RealMatrix rssLoc = MatrixUtils.createRealMatrix(rssLocation);
		RealMatrix magLoc = MatrixUtils.createRealMatrix(magneticLocation);

		RealMatrix rssCov = MatrixUtils.createRealMatrix(rssCovarianceMatrix);
		RealMatrix magCov = MatrixUtils.createRealMatrix(magneticCovarianceMatrix);

		try {
			RealMatrix rssInv = MatrixUtils.inverse(rssCov);
			RealMatrix magInv = MatrixUtils.inverse(magCov);

			RealMatrix sigma = MatrixUtils.inverse(rssInv.add(magInv));
			RealMatrix fusedLocationMatrix = sigma.multiply(rssInv.multiply(rssLoc).add(magInv.multiply(magLoc)));

			fusionEngineCovarianceMatrix = sigma.getData();
			fusionEngineLocation = fusedLocationMatrix.getData();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (Double.isNaN(fusionEngineLocation[0][0]) || Double.isNaN(fusionEngineLocation[1][0]))
			return null;
		return fusionEngineLocation;
	}

	double[][] getFusionEngineCovarianceMatrix() {
		return (fusionEngineLocation != null) ? fusionEngineCovarianceMatrix : null;
	}

	//Particle Filter
	private double[][] mean() {
		RealMatrix meanMatrix = MatrixUtils.createRealMatrix(2, 1);
		for (int i = 0; i < 2; i++) {
			double sum = 0;
			for (int j = 0; j < Globals.PARTICLES_NUMBER; j++) {
				sum += Globals.PARTICLES.get(j).location.getEntry(i, 0);
			}
			meanMatrix.setEntry(i, 0, sum / Globals.PARTICLES_NUMBER);
		}
		return meanMatrix.getData();
	}

	double[][] getParticleFilterLocation() {
		if (!is_pf_initialized) {
			if (fusionEngineLocation == null)
				return null;
			else {
				Globals.PARTICLES.clear();
				initializedParticleFilter();
				is_pf_initialized = true;
			}
		}

		double sumWeights = 0;
		RealMatrix fusedEngineLocationMatrix;
		fusedEngineLocationMatrix = MatrixUtils.createRealMatrix(fusionEngineLocation);
		RealMatrix particlesWeights = MatrixUtils.createRealMatrix(Globals.PARTICLES_NUMBER, 1);

		for (Particle particle : Globals.PARTICLES) {
			particle.predict(fusedEngineLocationMatrix);
			sumWeights += particle.weight;
		}

		for (int i = 0; i < Globals.PARTICLES_NUMBER; i++) {
			Globals.PARTICLES.get(i).update(sumWeights);
			particlesWeights.setEntry(i, 0, Globals.PARTICLES.get(i).weight);
		}

		particlesWeights = cumSum(particlesWeights);
		resampling(particlesWeights);
		particleFilterPredictedLocation = mean();

		if (Double.isNaN(particleFilterPredictedLocation[0][0]) || Double.isNaN(particleFilterPredictedLocation[1][0]))
			return null;
		return particleFilterPredictedLocation;
	}

	private void initializedParticleFilter() {
		Particle particle;
		double initialWeight = 1.0f / Globals.PARTICLES_NUMBER;
		RealMatrix basedLocation = MatrixUtils.createRealMatrix(fusionEngineLocation);

		int initializedParticles = 0;
		while (initializedParticles < Globals.PARTICLES_NUMBER) {
			particle = new Particle(basedLocation, initialWeight);
			if (!overlaps(particle.location.getEntry(0, 0), particle.location.getEntry(1, 0), particle.radius)) {
				Globals.PARTICLES.add(particle);
				Globals.GAME_STAGE.addActor(particle);
				initializedParticles++;
			} else {
				particle.eliminate();
			}
		}
	}

	private RealMatrix cumSum(RealMatrix matrix) {
		double sum = 0;
		RealMatrix cumulativeMatrix = MatrixUtils.createRealMatrix(matrix.getRowDimension(), matrix.getColumnDimension());
		for (int i = 0; i < matrix.getColumnDimension(); i++) {
			for (int j = 0; j < matrix.getRowDimension(); j++) {
				sum += matrix.getEntry(j, i);
				cumulativeMatrix.setEntry(j, i, sum);
			}
		}
		return cumulativeMatrix;
	}

	private boolean overlaps(double x, double y, float radius) {
		circle.x = (float) x;
		circle.y = (float) y;
		circle.radius = radius;
		center.set(circle.x, circle.y);
		float squareRadius = radius * radius;

		for (Polygon polygon : Globals.ROOM_POLYGONS) {
			if (polygon.contains(circle.x, circle.y)) return true;

			float[] polygonVertices = polygon.getVertices();
			for (int i = 0; i < polygonVertices.length; i += 2) {
				if (i == 0) {
					v1.set(polygonVertices[polygonVertices.length - 2], polygonVertices[polygonVertices.length - 1]);
					v2.set(polygonVertices[i], polygonVertices[i + 1]);
				} else {
					v1.set(polygonVertices[i - 2], polygonVertices[i - 1]);
					v2.set(polygonVertices[i], polygonVertices[i + 1]);
				}
				if (Intersector.intersectSegmentCircle(v1, v2, center, squareRadius)) return true;
			}
		}
		return false;
	}

	private void resampling(RealMatrix particlesWeights) {
		Random random = new Random();

		for (int i = 0; i < Globals.PARTICLES_NUMBER; i++) {
			double rand = random.nextDouble();
			for (int j = 0; j < Globals.PARTICLES_NUMBER; j++) {
				logicalArray[j] = rand <= particlesWeights.getEntry(j, 0);
			}

			int index = -1;
			for (int j = 0; j < Globals.PARTICLES_NUMBER; j++) {
				if (logicalArray[j]) {
					index = j;
					break;
				}
			}

			try {
				Globals.PARTICLES.get(i).location = Globals.PARTICLES.get(index).prediction;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (Particle p : Globals.PARTICLES) {
			p.weight = 1.0f / Globals.PARTICLES_NUMBER;
		}
	}

	//Independent
	void calculateCovarianceMatrix(String radioMap) {
		double[][] predictedLocation = null;
		ArrayList<Double> probabilities = null;
		ArrayList<Float[][]> coordinates = null;

		if (radioMap.equals("rss")) {
			predictedLocation = rssLocation;
			probabilities = rssProbabilities;
			coordinates = rssCoordinates;
		} else if (radioMap.equals("magnetic")) {
			predictedLocation = magneticLocation;
			probabilities = magneticProbabilities;
			coordinates = magneticCoordinates;
		}

		double x = 0;
		if (predictedLocation != null) {
			x = predictedLocation[0][0];
		}
		double y = 0;
		if (predictedLocation != null) {
			y = predictedLocation[1][0];
		}
		double[][] matrix = new double[2][2];

		if (probabilities != null) {
			for (int i = 0; i < probabilities.size(); i++) {
				float xi = coordinates.get(i)[0][0], yi = coordinates.get(i)[1][0];

				matrix[0][0] += probabilities.get(i) * Math.pow(xi - x, 2);
				matrix[0][1] += probabilities.get(i) * (xi - x) * (yi - y);
				matrix[1][0] += probabilities.get(i) * (yi - y) * (xi - x);
				matrix[1][1] += probabilities.get(i) * Math.pow(yi - y, 2);
			}
		}

		if (radioMap.equals("rss")) {
			rssCovarianceMatrix = matrix;
		} else if (radioMap.equals("magnetic")) {
			magneticCovarianceMatrix = matrix;
		}
	}

	private String readParameter(int algorithmChoice) {
		String parameter = null;

		if (algorithmChoice == 0) {
			parameter = rssRadioMap.getNaN();
		} else if (algorithmChoice == 1) {
			parameter = K;
		} else if (algorithmChoice == 2) {
			parameter = K;
		} else if (algorithmChoice == 3) {
			parameter = K;
		} else if (algorithmChoice == 4) {
			parameter = K;
		}

		return parameter;
	}

	private String calculateAverageKDistanceLocations(ArrayList<LocDistance> locDistanceResultsList, int K) {
		float x, y;
		float sumX = 0.0f;
		float sumY = 0.0f;
		String[] locationArray;
		int K_Min = Math.min(K, locDistanceResultsList.size());

		// Calculate the sum of X and Y
		for (int i = 0; i < K_Min; ++i) {
			locationArray = locDistanceResultsList.get(i).getLocation().split(" ");

			try {
				x = Float.parseFloat(locationArray[0].trim());
				y = Float.parseFloat(locationArray[1].trim());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			sumX += x;
			sumY += y;
		}

		// Calculate the average
		sumX /= K_Min;
		sumY /= K_Min;

		return sumX + " " + sumY;
	}

	private String calculateWeightedAverageProbabilityLocations(ArrayList<LocDistance> locDistanceResultsList, String radioMap) {
		double NP;
		float x, y;
		String[] locationArray;
		double weightedSumX = 0.0f;
		double weightedSumY = 0.0f;
		double sumProbabilities = 0.0f;

		// Calculate the sum of all probabilities
		for (LocDistance locDistance : locDistanceResultsList)
			sumProbabilities += locDistance.getDistance();

		// Calculate the weighted (Normalized Probabilities) sum of X and Y
		for (LocDistance locDistance : locDistanceResultsList) {
			locationArray = locDistance.getLocation().split(" ");

			try {
				x = Float.parseFloat(locationArray[0].trim());
				y = Float.parseFloat(locationArray[1].trim());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			NP = locDistance.getDistance() / sumProbabilities;

			if (radioMap.equals("rss")) {
				rssProbabilities.add(NP);
				rssCoordinates.add(new Float[][]{{x}, {y}});
			} else if (radioMap.equals("magnetic")) {
				magneticProbabilities.add(NP);
				magneticCoordinates.add(new Float[][]{{x}, {y}});
			}

			weightedSumX += (x * NP);
			weightedSumY += (y * NP);
		}
		return weightedSumX + " " + weightedSumY;
	}

	private String calculateWeightedAverageKDistanceLocations(ArrayList<LocDistance> locDistanceResultsList, int K, String radioMap) {
		float x, y;
		double locationWeight;
		double sumWeights = 0.0f;
		double weightedSumX = 0.0f;
		double weightedSumY = 0.0f;

		String[] locationArray;
		int K_Min = Math.min(K, locDistanceResultsList.size());

		// Calculate the weighted sum of X and Y
		for (int i = 0; i < K_Min; ++i) {
			if (locDistanceResultsList.get(i).getDistance() != 0.0) {
				locationWeight = 1 / locDistanceResultsList.get(i).getDistance();
			} else {
				locationWeight = 100;
			}
			locationArray = locDistanceResultsList.get(i).getLocation().split(" ");

			try {
				x = Float.parseFloat(locationArray[0].trim());
				y = Float.parseFloat(locationArray[1].trim());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			if (radioMap.equals("rss")) {
				rssCoordinates.add(new Float[][]{{x}, {y}});
				rssProbabilities.add(locationWeight);
			} else if (radioMap.equals("magnetic")) {
				magneticCoordinates.add(new Float[][]{{x}, {y}});
				magneticProbabilities.add(locationWeight);
			}

			sumWeights += locationWeight;
			weightedSumX += locationWeight * x;
			weightedSumY += locationWeight * y;
		}

		if (radioMap.equals("rss")) {
			for (int i = 0; i < rssProbabilities.size(); i++) {
				rssProbabilities.set(i, rssProbabilities.get(i) / (float) sumWeights);
			}
		} else if (radioMap.equals("magnetic")) {
			for (int i = 0; i < magneticProbabilities.size(); i++) {
				magneticProbabilities.set(i, magneticProbabilities.get(i) / (float) sumWeights);
			}
		}

		weightedSumX /= sumWeights;
		weightedSumY /= sumWeights;

		return weightedSumX + " " + weightedSumY;
	}
}