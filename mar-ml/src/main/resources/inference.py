import argparse
import os
from joblib import load
from flask import Flask
from flask import request
import nn as nn

# This comes from the training notebook
import re
from nltk.stem.porter import *
from nltk.corpus import stopwords
import nltk

MAR = os.environ['REPO_MAR']
if MAR is None:
    print("Variable REPO_MAR not defined")
    exit(-1)

ml_models = MAR + '/external-resources/ml-models/'

stemmer = PorterStemmer()
def custom_tokenizer(words):
    words = re.sub("[^0-9a-zA-Z]+", " ", words)
    tok = words.split(' ')
    return [stemmer.stem(t.lower()) for t in tok if (t!='' and t!=' ')]
    

app = Flask(__name__)
lvec = load(ml_models + 'vectorizer.joblib')
lnn = load(ml_models + 'nn.joblib')

@app.route('/category', methods=['GET'])
def category():
    # We need to wrap the text into a list (as in the notebook)
    input = [ request.args.get('text') ]
    category = lnn.predict(lvec.transform(input))[0]
    return category

@app.route('/tags', methods=['GET'])
def tags():
    input = request.args.get('text')
    tags = nn.getTags(input)
    return ' '.join(tags)

if __name__ == "__main__":
    app.run()
    
    
