# Create DeltaIoT scenarios correlation analysis
These instructions are for linux.
For windows, these instructions need to be adapted slightly.

## Install
Required: Python >= 3.12

Create virtual environment:
'''
python3 -m venv venv
'''

Install required libraries:
'''
venv/bin/pip install -r requirements.txt
'''

## Generate DelatIoT correlation data
Required raw simulation result data of the strategy as input for the correlation.

'''
./strategy_validation.sh correlate gen -r <correlation output file>.csv --strategy <strategy name> <path to resource folder>/resource/<strategy id>/kubernetes/tasks/Result_Task_*.json

e.g. for EAStrategy1d
./strategy_validation.sh correlate gen -r correlation_strategy1d.csv --strategy EASTRATEGY1D resource/DeltaIoT_modelled_ea_strategy1d/kubernetes/tasks/Result_Task_*.json
'''

## Calculate DelatIoT correlation
'''
./strategy_validation.sh correlate calc --correlation <correlation output file>.csv

e.g. for EAStrategy1d
./strategy_validation.sh correlate calc --correlation correlation_strategy1d.csv
'''
