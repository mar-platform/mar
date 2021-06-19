import json
import torch
import torch.nn as nn
import torch.nn.functional as F
import os
import os.path as osp
import glob
import json
import re

MAR = os.environ['REPO_MAR']
if MAR is None:
    print("Variable REPO_MAR not defined")
    exit(-1)

ml_models = MAR + '/external-resources/ml-models/'

def RepresentsInt(s):
    try: 
        int(s)
        return True
    except ValueError:
        return False

def camel_case_split(identifier):
    matches = re.finditer('.+?(?:(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|$)', identifier)
    return [m.group(0) for m in matches]

#do all process, camel case, lower etc.
def separation(word):
    if (RepresentsInt(word)):
        return [word]
    
    if '_' in word:
        words = word.split('_')
        to_return = [w.lower() for w in words if w!='']
        if to_return == []:
            return [word]
        else:
            return to_return
    
    wt = camel_case_split(word)
    flat_list = [w.lower() for w in wt]
    return flat_list

class Vocabulary(object):

    def __init__(self):
        
        self.word2id_names = {}
        self.id2word_names = {}

    
    def add_word(self, word):
        
        if not(word in self.word2id_names.keys()):
            x = len(self.id2word_names)
            self.word2id_names[word] = x
            self.id2word_names[x] = word
    
    def get_id(self,word):
        
        if word in self.word2id_names.keys():
            return self.word2id_names[word]
        else:
            return self.word2id_names['<unk>']
    
    def get_word(self,id):
        return self.id2word_names[id]
    
    def __len__(self):
        return len(self.word2id_names)
    
    def get_tensor(self,tokens):
        result = torch.zeros(len(self.word2id_names))
        for t in tokens:
            try:
                i = self.word2id_names[t]
                result[i] = result[i] + 1
            except:
                continue
        return result

tagsVocab = Vocabulary()
wordVocab = Vocabulary()

with open(ml_models + 'vocab_words.json', 'r') as fp:
    data = json.load(fp)
    wordVocab.word2id_names = data
    wordVocab.id2word_names = {y:x for x,y in data.items()}
    
with open(ml_models + 'vocab_tags.json', 'r') as fp:
    data = json.load(fp)
    tagsVocab.word2id_names = data
    tagsVocab.id2word_names = {y:x for x,y in data.items()}

    
class SimpleNN(nn.Module):
    
    def __init__(self):
        super(SimpleNN, self).__init__()
        self.lin1 = nn.Linear(len(wordVocab), 1024)
        self.lin2 = nn.Linear(1024, len(tagsVocab))
        
        #self.attention_vector = nn.Linear(hidden_dim,1,bias=False)
    
    def forward(self, x):
        return F.sigmoid(self.lin2(F.relu(self.lin1(x))))
    
def getTensorNew(s):
    tokens = tokenizeFile(s)
    return wordVocab.get_tensor(tokens)
    
def getBatchNew(strs):
    result = []
    for s in strs:
        result.append(getTensorNew(s))
    return torch.vstack(result)

def getIndicesNew(x):
    idx = []
    for i,a in enumerate(x):
        if a >= 1:
            idx.append(i)
    return idx

def tokenizeFile(string):
    ls = string.split(' ')
    result = []
    for w in ls:
        result = result + separation(w)
    return result

name_data = ml_models + 'multitag-modelset-ecore.data'
# Load the model
model2 = SimpleNN()

checkpoint = torch.load(name_data)
model2.load_state_dict(checkpoint['model_state_dict'])

epoch = checkpoint['epoch']
loss = checkpoint['loss']

model2.eval()    



def getTags(text):
    idx = getIndicesNew(torch.squeeze(model2(getBatchNew([text]))) > 0.5)
    result =[]
    for i in idx:
        result.append(tagsVocab.id2word_names[i])
    return result

