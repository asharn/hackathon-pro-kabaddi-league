
###### Documentation in progress ...

# Hackathon---Pro-Kabaddi-League for 2019 WC Prediction

![WC Logo](prokabaddi-logo.png)


## Goals

- Use Machine Learning to predict the winner of tournament, top teams, highest point for sucessesfull raid etc.

- Predict the outcome of individual matches for the entire competition.

- Run simulation of the next matches.

These goals present a unique real-world Machine Learning prediction problem and involve solving various Machine Learning tasks: data wrangling, feature extraction and outcome prediction.

## Data

We extracted the data set from site https://www.prokabaddi.com/

## Environment and tools

1. Jupyter Notebook
2. Numpy
3. Pandas
4. Seaborn
5. Matplotlib
6. Scikit-learn
7. Java
8. Eclipse

We used the previour yesrs dataset and a dataset containing the fixture of the group stages of the tournament. We compared Support Vector Machines, Logistic Regression, Random Forest and K-Nearest Neighbours model.
        
Random Forest was the winner with a training accuracy of 70 % and test accuracy of 67.5%.

## Installation

`pip install -r requirements.txt`

`jupyter notebook`

### According to this model XXX is likely to win this Tournament.

## Areas of further Improvement

1. Dataset - to improve dataset you could scrap data from the other website and also possibly use the players and teams data to assess the quality of each team player.

2. A confusion matrix would be great to analyse which games the model got wrong.

3. We could ensemble that is we could try stacking more models together to improve the accuracy.

Using Panda version `0.23.4`

Using Numpy version `0.10.1`

Using Python version 3.7.x

#### How to find Panda and Numpy version
print(pd.__version__)

