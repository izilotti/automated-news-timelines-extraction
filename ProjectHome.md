By applying Natural Language Processing, Machine Learning and some IR techniques, I am working on a research project to build up an automated framework to extract time and events from news articles, with the hope of promoting efficiencies of readers (help readers form a general knowledge about an unfamiliar news topic against a timeline in a relative short of time).

Simply put, I implemented four baselines and trained them with TimeBank Corpus: a Dictionary Lookup classifier, variants of Naive Bayes classifier, the `state-of-the-art' toolkit TARSQI, and the Conditional Random Fields by CRF++. The results show that CRFs outperform all other three significantly in terms of Precision, Accuracy and F-scores.

More importantly, to address the bottleneck of supervised learning algorithm - limited size of training data, I proposed a modified co-training framework on sequential text data, and tested it with various combinations of CRF learners. During co-training, one group of CRFs with specific features approaches 2% maximum improvements in F-scores by training on a large amount of unlabelled data.

Finally, by integrating the new labelled data into original training data set, there are noticeable improvements in Naive Bayes's predictions.

(The thesis about this project will be available soon.)


Oulin Yang, Master of Computing with Honours @ ANU

Supervised by: Dr. Scott Sanner @ NICTA.