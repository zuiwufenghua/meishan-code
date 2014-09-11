/*
 *        CRFsuite frontend.
 *
 * Copyright (c) 2007-2010, Naoaki Okazaki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the names of the authors nor the names of its contributors
 *       may be used to endorse or promote products derived from this
 *       software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/* $Id$ */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <vector>
#include <string>
#include "option.h"
#include "crfsuite.h"
#include "Instance.h"
#include "MyLib.h"
#include "MultiArray.h"

using namespace std;

#define    APPLICATION_S    "CRFSuite"

#define    SAFE_RELEASE(obj)    if ((obj) != NULL) { (obj)->release(obj); (obj) = NULL; }
#define    MAX(a, b)    ((a) < (b) ? (b) : (a))
#define    MATRIX(p, xl, x, y)        ((p)[(xl) * (y) + (x)])

class decode_chart 
{ 
public:
  double score;
  int ptag;
  int ptagkbest;

public:
  decode_chart()
  {
    score = DOUBLE_NEGATIVE_INFINITY;
    ptag = -2;
    ptagkbest = -2;
  }

  int compareTo(decode_chart other) {
    if(score < other.score - EPS)
      return -1;
    if(score > other.score + EPS)
      return 1;
    return 0;
  }
};

class kbestdecodechart { 
private:
    int size;  
    vector<decode_chart> theArray;
  
public:
    kbestdecodechart(int def_cap) 
    {
	    size = def_cap;
	    theArray.resize(def_cap); 
    } 
    
    decode_chart* get(int index)
    {
	    if(index >= 0 && index < size)
	    {
	        return &(theArray[index]);
	    }
	    else
	    {
	        return NULL;
	    }
    }
    
  
    void add(decode_chart e) 
    { 
	    if(e.compareTo(theArray[size-1]) < 0)
	    {
	        return;
	    }
	    int insertp = -1;
	
	    if (e.compareTo(theArray[0]) > 0) {
	        insertp = 0;
	    } 
      else 
      {
	      int prev = 0, end = size - 1, mid = -1;
	      while (prev < end - 1) 
        {
		      mid = (prev + end) / 2;
		      if (e.compareTo(theArray[mid]) > 0) 
          {
		          end = mid;
		      }
		      if (e.compareTo(theArray[mid]) <= 0) 
          {
		          prev = mid;
		      }
	      }
	      insertp = end;
	    }
	
	    for (int i = size - 1; i > insertp; i--) 
      {
	        theArray[i] = theArray[i-1];
	    }
	    theArray[insertp] = e;
    }
};

typedef struct {
    char *type;
    char *algorithm;
    char *model;
    int holdout;

    int num_params;
    char **params;
} learn_option_t;

typedef struct {
    char *model;
    int probability;
    int marginal;
    int kbest;

    int num_params;
    char **params;

} tagger_option_t;


static void learn_option_finish(learn_option_t* opt)
{
    int i;

    free(opt->model);

    for (i = 0;i < opt->num_params;++i) {
        free(opt->params[i]);
    }
    free(opt->params);
}


static void learn_option_init(learn_option_t* opt)
{
    memset(opt, 0, sizeof(*opt));
    opt->num_params = 0;
    opt->holdout = -1;
    opt->type = mystrdup("crf1d");
    opt->algorithm = mystrdup("lbfgs");
    opt->model = mystrdup("");
}

static void tagger_option_init(tagger_option_t* opt)
{
    memset(opt, 0, sizeof(*opt));
    opt->model = mystrdup("");
}

static void tagger_option_finish(tagger_option_t* opt)
{
    int i;

    free(opt->model);
    for (i = 0;i < opt->num_params;++i) {
        free(opt->params[i]);
    }
    free(opt->params);
}

class CRFInterFace
{
public:
  CRFInterFace(void)
  {
    bTrain = false;
    bTest = false;
  }

  ~CRFInterFace(void)
  {
  }

  bool bTrain;
  
  crfsuite_data_t data;
  crfsuite_trainer_t *trainer;
  //crfsuite_dictionary_t *attrs; 
  //crfsuite_dictionary_t *labels;
  learn_option_t opt;


  bool bTest;
  crfsuite_model_t* tagger_model;
  tagger_option_t tagger_opt;  
  crfsuite_tagger_t *tagger;
  crfsuite_dictionary_t *tagger_attrs; 
  crfsuite_dictionary_t *tagger_labels;
  int taggerL;
  crfsuite_instance_t tagger_inst;
  crfsuite_item_t tagger_item;
  crfsuite_attribute_t tagger_cont;
  
  int init_train_para(const string& modelFile)
  {
    if(bTrain || bTest)
    {
      printf("Previal model is not released!\n");
      return -1;
    }
    bTrain = true;

    learn_option_init(&opt);

    opt.model = mystrdup(modelFile.c_str());
    //opt.algorithm = mystrdup(strTrainAlgo.c_str());
    //opt.

    crfsuite_data_init(&data);
    crfsuite_create_instance("dictionary", (void**)&data.attrs);
    crfsuite_create_instance("dictionary", (void**)&data.labels);
    char trainer_id[128];
    sprintf(trainer_id, "train/%s/%s", opt.type, opt.algorithm);
    if(!crfsuite_create_instance(trainer_id, (void**)&trainer))
    {
      return -1;
    }

    for (int i = 0;i < opt.num_params;++i) {
        char *value = NULL;
        char *name = opt.params[i];
        crfsuite_params_t* params = trainer->params(trainer);
        
        /* Split the parameter argument by the first '=' character. */
        value = strchr(name, '=');
        if (value != NULL) {
            *value++ = 0;
        }

        if (params->set(params, name, value) != 0) {
            printf("ERROR: paraneter not found: %s\n", name);
            return -1;
        }
        params->release(params);
    }

    return 1;
  }


  void init_data(const vector<Instance>& m_vecInstances, int group)
  {
    int n = 0;
    int lid = -1;
    crfsuite_instance_t inst;
    crfsuite_item_t item;
    crfsuite_attribute_t cont;
    crfsuite_dictionary_t *attrs = data.attrs;
    crfsuite_dictionary_t *labels = data.labels;
    crfsuite_instance_init(&inst);
    inst.group = group;

    for(int i=0;i<m_vecInstances.size();i++)
	  {
      lid = -1;
      crfsuite_item_init(&item);
      for(int k=0; k<m_vecInstances[i].features.size(); k++)
      {
        lid = labels->get(labels, m_vecInstances[i].labels[k].c_str());
        for(int curf = 0; curf < m_vecInstances[i].features[k].size(); curf++)
        {
          crfsuite_attribute_init(&cont);
          cont.aid = attrs->get(attrs, m_vecInstances[i].features[k][curf].c_str());
          cont.value = 1.0;
          crfsuite_item_append_attribute(&item, &cont);
        }
        if (0 <= lid) 
        {
            crfsuite_instance_append(&inst, &item, lid);
        }
        crfsuite_item_finish(&item);
      }

      crfsuite_data_append(&data, &inst);
      crfsuite_instance_finish(&inst);
      inst.group = group;
    }
  }

  void crf_train(const vector<Instance>& m_vecInstances, const vector<Instance>& m_vecInstancesDev)
  {
    if(!bTrain) 
    {
      printf("Not Initizlize for train.\n");
      return;
    }
    init_data(m_vecInstances, 0);
    init_data(m_vecInstancesDev, 1);

    printf("Statistics the data set(s)\n");
    printf("Number of instances: %d\n", data.num_instances);
    printf("Number of items: %d\n", crfsuite_data_totalitems(&data));
    printf("Number of attributes: %d\n", data.attrs->num(data.attrs));
    printf("Number of labels: %d\n", data.labels->num(data.labels));
    printf("\n");
    fflush(stdout);

    opt.holdout = 1;
    trainer->set_message_callback(trainer, NULL, message_callback);
    int ret = trainer->train(trainer, &data, opt.model, opt.holdout);
  }

  void release_train()
  {
    if(!bTrain) 
    {
      printf("Not Initizlize for train.\n");
      return;
    }
    bTrain = false;
    SAFE_RELEASE(trainer);
    SAFE_RELEASE(data.labels);
    SAFE_RELEASE(data.attrs);

    crfsuite_data_finish(&data);
    learn_option_finish(&opt);
  }


	// decodeType > 0, KBest output; decodeType == 0, marginal output;
  int init_test_para(const string& modelFile, const int decodeType)
  {
    if(bTrain || bTest)
    {
      printf("Previal model is not released!\n");
    }
    bTest = true;
    int ret = 0;
    tagger_option_init(&tagger_opt);
		if(decodeType > 0) {
				tagger_opt.kbest = decodeType;
				tagger_opt.marginal = 0;
				tagger_opt.probability = 0;
		}
		else if(decodeType == 0)
		{
				tagger_opt.kbest = 0;
				tagger_opt.marginal = 1;
				tagger_opt.probability = 0;
		}
		else
		{
				tagger_opt.kbest = -decodeType;
				tagger_opt.marginal = 0;
				tagger_opt.probability = 1;
		}

    tagger_opt.model = mystrdup(modelFile.c_str());
    if (tagger_opt.model != NULL) {
        /* Create a model instance corresponding to the model file. */     
      if (ret = crfsuite_create_instance_from_file(tagger_opt.model, (void**)&tagger_model)) {
          return -1;
      }
    }

    /* Obtain the dictionary interface representing the labels in the model. */
    if (ret = tagger_model->get_labels(tagger_model, &tagger_labels)) {
        return -1;
    }

    /* Obtain the dictionary interface representing the attributes in the model. */
    if (ret = tagger_model->get_attrs(tagger_model, &tagger_attrs)) {
        return -1;
    }

    /* Obtain the tagger interface. */
    if (ret = tagger_model->get_tagger(tagger_model, &tagger)) {
        return -1;
    }

    /* Initialize the objects for instance and evaluation. */
    taggerL = tagger_labels->num(tagger_labels);
    crfsuite_instance_init(&tagger_inst);
  }


  int ctf_test(vector<vector<string> > features, 
    vector<vector<string> >& tags, 
    vector<double>& probs, 
    vector<vector<double> >& margin_probs)
  {
    if(!bTest) 
    {
      printf("Not Initizlize for test.\n");
      return -1;
    }


    int lid = -1;
    
    for(int k=0; k<features.size(); k++)
    {
      crfsuite_item_init(&tagger_item);
      lid = taggerL;
      for(int curf = 0; curf < features[k].size(); curf++)
      {
        int aid = tagger_attrs->to_id(tagger_attrs, features[k][curf].c_str());
        if (0 <= aid) 
        {
            /* Associate the attribute with the current item. */
          crfsuite_attribute_set(&tagger_cont, aid, 1.0);
          crfsuite_item_append_attribute(&tagger_item, &tagger_cont);
        }
      }

      crfsuite_instance_append(&tagger_inst, &tagger_item, lid);
      crfsuite_item_finish(&tagger_item);
    }

    if (crfsuite_instance_empty(&tagger_inst)) 
    {
      return -1;
    }

    int ret = 0;
    floatval_t *state; 
    floatval_t *trans;
		int all_tag_num = tagger_labels->num(tagger_labels);
    if ((ret = tagger->set(tagger, &tagger_inst, &state, &trans))) 
    {
        return -1;
    }
    
		tags.clear();
		probs.clear();
		margin_probs.clear();

		if(tagger_opt.kbest > 0)
		{
				tags.resize(tagger_opt.kbest);
				probs.resize(tagger_opt.kbest);
				margin_probs.resize(tagger_opt.kbest);

				for(int k = 0; k < tagger_opt.kbest; k++)
				{
						tags[k].resize(tagger_inst.num_items);
						margin_probs[k].resize(tagger_inst.num_items);
				}
		}
		else
		{			
				tags.resize(all_tag_num);
				margin_probs.resize(all_tag_num);
				for(int k = 0; k < all_tag_num; k++)
				{
						tags[k].resize(tagger_inst.num_items);
						margin_probs[k].resize(tagger_inst.num_items);
				}
		}


    if(tagger_opt.kbest == 1)
    {
				floatval_t score = 0;
				int *output = (int*)calloc(sizeof(int), tagger_inst.num_items);
				if ((ret = tagger->viterbi(tagger, output, &score))) 
				{
						return -1;
				}
				floatval_t lognorm;
				tagger->lognorm(tagger, &lognorm);
				probs[0] = exp(score - lognorm);
      
				for (int i = 0;i < tagger_inst.num_items; ++i) 
				{
						const char *label = NULL;
						floatval_t prob;
						tagger->marginal_point(tagger, output[i], i, &prob);
						margin_probs[0][i] = prob;
						tagger_labels->to_string(tagger_labels, output[i], &label);
						tags[0][i] = string(label);
						tagger_labels->free(tagger_labels, label);
				}    
				free(output);
    }
    else if(tagger_opt.kbest > 1)
    {
				kbest_decoder(state, trans, tags, probs, margin_probs);
    }
		else
		{
				floatval_t score = 0;
				int *output = (int*)calloc(sizeof(int), tagger_inst.num_items);
				if ((ret = tagger->viterbi(tagger, output, &score))) 
				{
          return -1;
				}
			
				const char *label = NULL;
				for (int i = 0;i < tagger_inst.num_items;++i) 
				{				
						for(int j = 0; j < tagger_labels->num(tagger_labels); j++)
						{
								floatval_t prob;
								tagger->marginal_point(tagger, j, i, &prob);
								margin_probs[j][i] = prob;
								tagger_labels->to_string(tagger_labels, j, &label);
								tags[j][i] = string(label);
								tagger_labels->free(tagger_labels, label);
						}
				}
				free(output);
		}

    
    crfsuite_instance_finish(&tagger_inst);
  }


  void kbest_decoder(floatval_t *state, floatval_t *trans,
    vector<vector<string> >& tags, vector<double>& probs, 
    vector<vector<double> >& margin_probs)
  {

      vector<vector<double> > matrix_scores;
      vector<vector<double> > trans_scores;
      int instSize = tagger_inst.num_items;

      matrix_scores.resize(instSize);
      int labelNum = tagger_labels->num(tagger_labels);
      for (int i = 0;i < instSize;++i) 
      {        
        matrix_scores[i].resize(labelNum);
        for(int labelId = 0; labelId < labelNum; labelId++)
        {
          matrix_scores[i][labelId] = MATRIX(state, labelNum, labelId, i);
        }
      }

      trans_scores.resize(labelNum);
      for (int i = 0;i < labelNum;++i) 
      {        
        trans_scores[i].resize(labelNum);
        for(int labelId = 0; labelId < labelNum; labelId++)
        {
          trans_scores[i][labelId] = MATRIX(trans, labelNum, labelId, i);
        }
      }

      vector<unsigned int> chart_dim;
      unsigned int chart_pos;
      MultiArray<decode_chart> decode_score_chart;
      int kbestdecode = tagger_opt.kbest;
      decode_score_chart.setDemisionVal(chart_dim, instSize, labelNum, kbestdecode);
      decode_score_chart.resize(chart_dim, decode_chart());

      for(int i = 0; i < instSize; i++)
      {
        for(int j1 = 0; j1 < labelNum; j1++)
        {      
          if(i == 0)
          {
            decode_chart current_chart;
            current_chart.score = matrix_scores[0][j1];
            current_chart.ptag = -1;
            current_chart.ptagkbest = -1;
            decode_score_chart.setDemisionVal(chart_dim, 0, j1, 0) ;
            decode_score_chart.getElement(chart_dim, chart_pos) = current_chart;        
            continue;
          }
          kbestdecodechart * heap = new kbestdecodechart(kbestdecode);
          for(int j2 = 0; j2 < labelNum; j2++)
          {
            for(int k = 0; k < kbestdecode; k++)
            {
              decode_score_chart.setDemisionVal(chart_dim, i-1, j2, k) ;
              decode_chart current_chart;
              current_chart.ptag = j2;
              current_chart.ptagkbest = k;
              current_chart.score = decode_score_chart.getElement(chart_dim, chart_pos).score;
              if(current_chart.ptag != -2)
              {
                current_chart.score = current_chart.score + matrix_scores[i][j1] + trans_scores[j2][j1];
                heap->add(current_chart);
              }
              else
              {
                break;
              }
            }
          }

          for(int k = 0; k < kbestdecode; k ++)
	        {
            decode_chart current_chart = *(heap->get(k));
            if(current_chart.ptag != -2)
            {
              decode_score_chart.setDemisionVal(chart_dim, i, j1, k);
              decode_score_chart.getElement(chart_dim, chart_pos) = current_chart;
            }
            else
            {
              break;
            }
	        }
          delete heap;
        }
      }

      kbestdecodechart* heap = new kbestdecodechart(kbestdecode);
 
      for(int j = 0; j < labelNum; j++)
      {
        for(int k = 0; k < kbestdecode; k++)
        {
          decode_score_chart.setDemisionVal(chart_dim, instSize-1, j, k) ;
          decode_chart current_chart;
          current_chart.ptag = j;
          current_chart.ptagkbest = k;
          current_chart.score = decode_score_chart.getElement(chart_dim, chart_pos).score;
      
          if(current_chart.ptag != -2 )
          {
            heap->add(current_chart);
          }
          else
          {
            break;
          }
        }
      }

      for(int k = 0; k < kbestdecode; k++)
      {
        decode_chart current_chart = *(heap->get(k));
        int cur_index = instSize;
        int prev_index = instSize -1;
        floatval_t score = 0;
        vector<int> output;
        score = current_chart.score;
        probs[k] = 1;
        if(current_chart.ptag == -2)
        {
          probs[k] = -1;
          continue;
        }

        while(current_chart.ptag != -2 && prev_index >= 0)
        {
          int ptag = current_chart.ptag;
          int ptagkbest = current_chart.ptagkbest;
          output.insert(output.begin(), ptag);
          prev_index--;
          cur_index--;
          decode_score_chart.setDemisionVal(chart_dim, cur_index, ptag, ptagkbest);
          current_chart = decode_score_chart.getElement(chart_dim, chart_pos);
        }

        if(prev_index == -1)
        {
						floatval_t lognorm;
						tagger->lognorm(tagger, &lognorm);
						probs[k] = exp(score - lognorm);
						for (int i = 0;i < tagger_inst.num_items;++i) 
						{
								const char *label = NULL;
								floatval_t prob;
								tagger->marginal_point(tagger, output[i], i, &prob);
								margin_probs[k][i] = prob;
								tagger_labels->to_string(tagger_labels, output[i], &label);
								tags[k][i] = string(label);
								tagger_labels->free(tagger_labels, label);
						}
        }
      }

      delete heap;
  }


  void release_test()
  {
    if(!bTest) 
    {
      printf("Not Initizlize for test.\n");
      return;
    }
    bTest = false;

    crfsuite_instance_finish(&tagger_inst);

    SAFE_RELEASE(tagger);
    SAFE_RELEASE(tagger_attrs);
    SAFE_RELEASE(tagger_labels);

    SAFE_RELEASE(tagger_model);
    tagger_option_finish(&tagger_opt);
  }

      
};