# Create DeltaIoT scenarios results tables
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

## Generate result table
Required raw simulation result data of the strategy as input to generate the table for.

'''
./validate_simexp.sh generate -r <result output file>.json -s <strategy name> --no_validation <path to resource folder>/resource/<strategy id>/generations.json

e.g. for EAStrategy1d:
./validate_simexp.sh generate -r result_1d.json -s EAStrategy1d --no_validation resource/DeltaIoT_modelled_ea_strategy1d/generations.json
'''

## Show result table
'''
./validate_simexp.sh show -r <result output file>.json

e.g. for EAStrategy1d:
./validate_simexp.sh show -r result_1d.json
'''
