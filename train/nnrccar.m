%% NNRCCar - training code.
%
% This code works with a solution to Exercise 4 of the ml-class.org machine learning course.
% Since the terms of use of the ml class exercises do not permit further distribution, I
% can't share that code here. [http://www.ml-class.org/course/auth/tos]
% To use, copy nnrccar.m into the directory where you have already completed
% exercise 4.

%% Initialization
clear ; close all; clc

input_layer_size  = 25344;  % 25344 pixel images
hidden_layer_size = 64;     % 64 hidden units
num_labels = 4;             % 4 labels: left, right, fwd, back

% y.dat contains the control labels from a human driving the car corresponding to
% video frames in X.dat.  y.dat and X.dat are written by the parse.py script.
load('y.dat');
tic
load('X.dat');
toc

% Since the frames were sent as signed bytes, we need to convert back to unsigned values.
% This vectorized implementation is much faster than a for loop.
x = ((x < 0) .* (x + 256)) + ((x >= 0) .* (x));

X = x(:, 1:input_layer_size);
y = y(:, 2:end);
m = size(X, 1);

% Randomly select 8 data points to display
sel = randperm(size(X, 1));
sel = sel(1:8);

% To use displayData, you need to modify the displayData function to expect 176x144 px samples.
%displayData(X(sel, 1:end));

initial_Theta1 = randInitializeWeights(input_layer_size, hidden_layer_size);
initial_Theta2 = randInitializeWeights(hidden_layer_size, num_labels);

% Unroll parameters
initial_nn_params = [initial_Theta1(:) ; initial_Theta2(:)];

fprintf('\nTraining Neural Network... \n')
options = optimset('MaxIter', 100);
lambda = 1000;

% Create "short hand" for the cost function to be minimized
costFunction = @(p) nnCostFunction(p, ...
                                   input_layer_size, ...
                                   hidden_layer_size, ...
                                   num_labels, X, y, lambda);

% Now, costFunction is a function that takes in only one argument (the
% neural network parameters)
[nn_params, cost] = fmincg(costFunction, initial_nn_params, options);

% Obtain Theta1 and Theta2 back from nn_params
Theta1 = reshape(nn_params(1:hidden_layer_size * (input_layer_size + 1)), ...
                 hidden_layer_size, (input_layer_size + 1));

Theta2 = reshape(nn_params((1 + (hidden_layer_size * (input_layer_size + 1))):end), ...
                 num_labels, (hidden_layer_size + 1));

% To use displayData, you need to modify the displayData function to expect 176x144 px samples.
%displayData(Theta1(:, 2:end));

pred = predict(Theta1, Theta2, X);

fprintf('\nTraining Set Accuracy: %f\n', mean(double(pred == y)) * 100);

save "Theta1.dat" Theta1
save "Theta2.dat" Theta2
