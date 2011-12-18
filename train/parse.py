#!/bin/python
"""
parse.py is a simple Python script which converts a feature file written by the Driver GUI
application (part of NNRCCar) into octave matrix files suitable for training the neural network.

This script expects to find the input file nnrccar.features in the current directory.  It outputs
files y.dat and X.dat which are expected input to the octave script nnrcar.m
"""

if __name__ == '__main__':
	features = file('nnrccar.features', 'r')
	data = features.readlines()
	lines = 0
	c = 0
	for line in data:
		if (c % 2) == 0 and "1" in line:
			lines = lines + 1
		c = c + 1
	feature_count = lines #len(data) / 2
	ys = file('y.dat', 'w')
	ys.writelines(['# name: y\n', '# type: matrix\n', '# rows: %d\n' % feature_count, '# columns: 5\n'])
	xs = file('X.dat', 'w')
	xs.writelines(['# name: x\n', '# type: matrix\n', '# rows: %d\n' % feature_count, '# columns: 25351\n'])
	c = 0
	write = False
	for line in data:
		if (c % 2) == 0:
			write = ("1" in line)
			if write:
				ys.write(line)
		else:
			if write:
				xs.write(line)
		c = c + 1
	features.close()
	xs.close()
	ys.close()
