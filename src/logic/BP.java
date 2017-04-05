package logic;

import java.io.Serializable;
import java.util.Random;

public class BP implements Serializable{
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	/**
	 * input vector.
	 */
	private final double[] input;
	/**
	 * hidden layer.
	 */
	private final double[] hidden;
	/**
	 * output layer.
	 */
	private final double[] output;
	/**
	 * target.
	 */
	private final double[] target;

	/**
	 * delta vector of the hidden layer .
	 */
	private final double[] hidDelta;
	/**
	 * output layer of the output layer.
	 */
	private final double[] optDelta;

	/**
	 * learning rate.
	 */
	private final double eta;
	/**
	 * momentum.
	 */
	private final double momentum;

	/**
	 * weight matrix from input layer to hidden layer.
	 */
	private final double[][] iptHidWeights;
	/**
	 * weight matrix from hidden layer to output layer.
	 */
	private final double[][] hidOptWeights;

	/**
	 * previous weight update.
	 */
	private final double[][] iptHidPrevUptWeights;    
	/**
	 * previous weight update.
	 */
	private final double[][] hidOptPrevUptWeights;

	public double optErrSum = 0d;

	public double hidErrSum = 0d;

	private final Random random;

	public BP(int inputSize, int hiddenSize, int outputSize, double eta,
			double momentum) {

		input = new double[inputSize + 1];
		hidden = new double[hiddenSize + 1];
		output = new double[outputSize + 1];
		target = new double[outputSize + 1];

		hidDelta = new double[hiddenSize + 1];
		optDelta = new double[outputSize + 1];

		iptHidWeights = new double[inputSize + 1][hiddenSize + 1];
		hidOptWeights = new double[hiddenSize + 1][outputSize + 1];

		random = new Random(19881211);
		randomizeWeights(iptHidWeights);
		randomizeWeights(hidOptWeights);

		iptHidPrevUptWeights = new double[inputSize + 1][hiddenSize + 1];
		hidOptPrevUptWeights = new double[hiddenSize + 1][outputSize + 1];

		this.eta = eta;           //learning rate,which controls how much the weights are adjusted at each update.
		this.momentum = momentum; //Momentum tends to keep the weight changes moving in a consistent direction
	}
	//initialize weights
	private void randomizeWeights(double[][] matrix) {
		for (int i = 0, len = matrix.length; i != len; i++)
			for (int j = 0, len2 = matrix[i].length; j != len2; j++) {
				double real = random.nextDouble();
			
				matrix[i][j] = real-0.5;
			}
	}

	public BP(int inputSize, int hiddenSize, int outputSize) {
		this(inputSize, hiddenSize, outputSize, 0.5, 0.9);
	}

	public double[] train(double[] trainData, double[] target) {
		loadInput(trainData);
		loadTarget(target);
		double[] d = forward();
		calculateDelta();
		adjustWeight();
		return d;
	}

	public double[] test(double[] inData) {
		if (inData.length != input.length - 1) {
			throw new IllegalArgumentException("Size Do Not Match.");
		}
		System.arraycopy(inData, 0, input, 1, inData.length);
		forward();
		return getNetworkOutput();
	}

	/**
	 * Return the output layer.
	 * 
	 * @return
	 */
	private double[] getNetworkOutput() {
		int len = output.length;
		double[] temp = new double[len - 1];
		for (int i = 1; i != len; i++)
			temp[i - 1] = output[i];
		return temp;
	}

	/**
	 * Load the target data.
	 * 
	 * @param arg
	 */
	private void loadTarget(double[] arg) {
		System.out.println("arg.length="+arg.length+" target.length="+target.length);
		if (arg.length != target.length - 1) {
			throw new IllegalArgumentException("Size Do Not Match.");
		}
		System.arraycopy(arg, 0, target, 1, arg.length);
	}

	/**
	 * Load the training data.
	 * 
	 * @param inData
	 */
	private void loadInput(double[] inData) {
		if (inData.length != input.length - 1) {
			throw new IllegalArgumentException("Size Do Not Match.");
		}
		System.arraycopy(inData, 0, input, 1, inData.length);
	}

	/**
	 * Forward.
	 * 
	 * @param layer0
	 * @param layer1
	 * @param weight
	 */
	
	//hidden layer 
	//threshold 
	private double[] forward(double[] layer0, double[] layer1, double[][] weight) {
		// threshold unit.
		layer0[0] = 0.0;
		
		for (int j = 1, len = layer1.length; j != len; ++j) {
			
			double sum = 0;
			/*System.out.println(layer0.length);
			for(int x=0;x<20;x++){
				for(int y=0;y<20;y++)
				{
					System.out.print(layer0[x*20+y]);
				}
				System.out.println();
			}
			*/
			for (int i = 0, len2 = layer0.length; i != len2; ++i){
				//System.out.println("layer0= "+layer0[i]);
				sum += weight[i][j] * layer0[i];}
			layer1[j] = sigmoid(sum);
//			System.out.println(layer1[j]);
		}
//		System.out.println("===========================================");
		return layer1;
	}

	/**
	 * Forward.
	 */
	private double[] forward() {
		forward(input, hidden, iptHidWeights);
		return forward(hidden, output, hidOptWeights);
	}

	/**
	 * Calculate output error.
	 */
	
	
	
	private void outputErr() {
		double errSum = 0;
		for (int idx = 1, len = optDelta.length; idx != len; ++idx) {
			double o = output[idx];
			optDelta[idx] = o * (1d - o) * (target[idx] - o);
			errSum += Math.abs(optDelta[idx]);
		}
		optErrSum = errSum;
	}

	/**
	 * Calculate hidden errors.
	 */
	private void hiddenErr() {
		double errSum = 0;
		for (int j = 1, len = hidDelta.length; j != len; ++j) {
			double o = hidden[j];
			double sum = 0;
			for (int k = 1, len2 = optDelta.length; k != len2; ++k)
				sum += hidOptWeights[j][k] * optDelta[k];
			hidDelta[j] = o * (1d - o) * sum;
			errSum += Math.abs(hidDelta[j]);
		}
		hidErrSum = errSum;
	}

	/**
	 * Calculate errors of all layers.
	 */
	private void calculateDelta() {
		outputErr();
		hiddenErr();
	}

	/**
	 * Adjust the weight matrix.
	 * 
	 * @param delta
	 * @param layer
	 * @param weight
	 * @param prevWeight
	 */
	
	///trouble
	
	private void adjustWeight(double[] delta, double[] layer,
			double[][] weight, double[][] prevWeight) {

		layer[0] = 0;
		for (int i = 1, len = delta.length; i != len; ++i) {
			for (int j = 1, len2 = layer.length; j != len2; ++j) {
				double newVal =eta * delta[i]* layer[j];
				weight[j][i] += newVal;
				prevWeight[j][i] = newVal;
			}
		}
//		System.out.println("OK1");
	}

	/**
	 * Adjust all weight matrices.
	 */
	private void adjustWeight() {
		adjustWeight(optDelta, hidden, hidOptWeights, hidOptPrevUptWeights);
		adjustWeight(hidDelta, input, iptHidWeights, iptHidPrevUptWeights);
	}

	/**
	 * Sigmoid.
	 * 
	 * @param val
	 * @return
	 */
	
	
	private double sigmoid(double val) {
		return 1d / (1d + Math.exp(-val));
	}
}
