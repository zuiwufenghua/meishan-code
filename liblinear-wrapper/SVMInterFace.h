#ifndef _SVMINTERFACE_
#define _SVMINTERFACE_
#pragma once
#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <errno.h>
#include "linear.h"
#define Malloc(type,n) (type *)malloc((n)*sizeof(type))
#define INF HUGE_VAL
#include "JSTInstance.h"
#include "common.h"
#include "JSTOptions.h"
#include "FeatureVec.h"

// Max Heap
// We know that never more than K elements on Heap
class HeapFeature { 
private:
    int size;  
    vector<Feature> theArray;
  
public:
    HeapFeature(int def_cap) 
    {
	    size = def_cap;
	    theArray.resize(def_cap); 

    } 
    
    Feature* get(int index)
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
    
  
    void add(Feature e) 
    { 
	    if(e.compareTo(theArray[size-1]) > 0)
	    {
	        return;
	    }
	    int insertp = -1;
	
	    if (e.compareTo(theArray[0]) < 0) {
	        insertp = 0;
	    } 
      else 
      {
	      int prev = 0, end = size - 1, mid = -1;
	      while (prev < end - 1) 
        {
		      mid = (prev + end) / 2;
		      if (e.compareTo(theArray[mid]) < 0) 
          {
		          end = mid;
		      }
		      if (e.compareTo(theArray[mid]) >= 0) 
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

class SVMInterFace
{
public:
  SVMInterFace(void);
  ~SVMInterFace(void);

public:
  //train
  struct feature_node *x_space;
  struct parameter param;
  struct problem prob;
  struct model* model_;
  int flag_cross_validation;
  int nr_fold;
  double bias;

  //predict
  int nr_class;
  int nr_feature;
  

public:
  void svm_train()
  {
    const char* error_msg = check_parameter(&prob,&param);

	  if(error_msg)
	  {
		  fprintf(stderr,"Error: %s\n",error_msg);
		  exit(1);
	  }
    model_=train(&prob, &param);
  }

  void save_svm_model(const char* model_file_name)
  {
    if(save_model(model_file_name, model_))
		{
			fprintf(stderr,"can't save model to file %s\n",model_file_name);
			exit(1);
		}
  }

  void load_svm_model(const char* model_file_name)
  {
    if((model_=load_model(model_file_name))==0)
	  {
		  fprintf(stderr,"can't open model file %s\n",model_file_name);
		  exit(1);
	  }
  }

  void release_all()
  {
    free_and_destroy_model(&model_);
    destroy_param(&param);
    if(prob.y)
    {
	    free(prob.y);
    }
    if(prob.x)
    {
	    free(prob.x);
    }
    if(x_space)
    {
	    free(x_space);
    }
  }

  void release()
  {
    destroy_param(&param);
    if(prob.y)
    {
	    free(prob.y);
    }
    if(prob.x)
    {
	    free(prob.x);
    }
    if(x_space)
    {
	    free(x_space);
    }
  }

  void free_model()
  {
    free_and_destroy_model(&model_);
  }


  int svm_predict(const FeatureVec& fv)
  {
    int n;
    if(model_->bias>=0)
		  n=nr_feature+1;
	  else
		  n=nr_feature;

    int feature_size = fv.m_fv.size();
    HeapFeature* heap = new HeapFeature(feature_size);
  
    int curf;
    for(curf = 0; curf < feature_size; curf++)
    {
      heap->add(fv.m_fv[curf]);      
    }

    struct feature_node *x;
    
    x = Malloc(struct feature_node,feature_size+2);
    for(curf = 0; curf < feature_size; curf++)
    {
      x[curf].index = heap->get(curf)->index+1;
      x[curf].value = heap->get(curf)->value;
    }

    delete heap;

    if(model_->bias >= 0)
		{
			x[curf].index = n;
			x[curf].value = model_->bias;
			curf++;
		}
		x[curf].index = -1;

    int predict_label = predict(model_,x);
    free(x);

    return predict_label;
  }




public:
  void print_null(const char *s) {}
  void exit_with_help_for_train()
  {
	  printf(
	  "Usage: train [options] training_set_file [model_file]\n"
	  "options:\n"
	  "-s type : set type of solver (default 1)\n"
	  "	0 -- L2-regularized logistic regression (primal)\n"
	  "	1 -- L2-regularized L2-loss support vector classification (dual)\n"	
	  "	2 -- L2-regularized L2-loss support vector classification (primal)\n"
	  "	3 -- L2-regularized L1-loss support vector classification (dual)\n"
	  "	4 -- multi-class support vector classification by Crammer and Singer\n"
	  "	5 -- L1-regularized L2-loss support vector classification\n"
	  "	6 -- L1-regularized logistic regression\n"
	  "	7 -- L2-regularized logistic regression (dual)\n"
	  "-c cost : set the parameter C (default 1)\n"
	  "-e epsilon : set tolerance of termination criterion\n"
	  "	-s 0 and 2\n" 
	  "		|f'(w)|_2 <= eps*min(pos,neg)/l*|f'(w0)|_2,\n" 
	  "		where f is the primal function and pos/neg are # of\n" 
	  "		positive/negative data (default 0.01)\n"
	  "	-s 1, 3, 4 and 7\n"
	  "		Dual maximal violation <= eps; similar to libsvm (default 0.1)\n"
	  "	-s 5 and 6\n"
	  "		|f'(w)|_1 <= eps*min(pos,neg)/l*|f'(w0)|_1,\n"
	  "		where f is the primal function (default 0.01)\n"
	  "-B bias : if bias >= 0, instance x becomes [x; bias]; if < 0, no bias term added (default -1)\n"
	  "-wi weight: weights adjust the parameter C of different classes (see README for details)\n"
	  "-v n: n-fold cross validation mode\n"
	  "-q : quiet mode (no outputs)\n"
	  );
	  exit(1);
  }


  void initTrainParam(const JSTOptions &options)
  {
    void (*print_func)(const char*) = NULL;	// default printing to stdout
	  // default values
	  //param.solver_type = L2R_L2LOSS_SVC_DUAL;
    param.solver_type = MCSVM_CS;
	  param.C = 1;
	  param.eps = INF; // see setting below
	  param.nr_weight = 0;
	  param.weight_label = NULL;
	  param.weight = NULL;
	  flag_cross_validation = 0;
	  bias = -1;

	  // parse options
	  
	  set_print_string_function(print_func);

	  if(param.eps == INF)
	  {
		  if(param.solver_type == L2R_LR || param.solver_type == L2R_L2LOSS_SVC)
			  param.eps = 0.01;
		  else if(param.solver_type == L2R_L2LOSS_SVC_DUAL || param.solver_type == L2R_L1LOSS_SVC_DUAL || param.solver_type == MCSVM_CS || param.solver_type == L2R_LR_DUAL)
			  param.eps = 0.1;
		  else if(param.solver_type == L1R_L2LOSS_SVC || param.solver_type == L1R_LR)
			  param.eps = 0.01;
	  }
  }


  void resetC(double C)
  {
    param.C = C;
  }

  void init_problems(const vector<JSTInstance>& m_vecInstances, int max_index)
  {
    int i, k,curp,curf;
	  long int elements, j;
	  char *endptr;
	  char *idx, *val, *label;
    prob.l = 0;
	  elements = 0;

    for(i=0;i<m_vecInstances.size();i++)
	  {
      for(k=0; k<m_vecInstances[i].sentenceTagIds.size(); k++)
      {
        prob.l++;
        elements += m_vecInstances[i].fvs[k].m_fv.size() + 1;
      }
    }

    prob.bias=bias;

    prob.y = Malloc(int,prob.l);
    prob.x = Malloc(struct feature_node *,prob.l);
    x_space = Malloc(struct feature_node,elements+prob.l);

	  j=0;
    curp=0;
    //ofstream fout("train.corpus.libsvm");
    for(i=0;i<m_vecInstances.size();i++)
	  {
      for(k=0; k<m_vecInstances[i].sentenceTagIds.size(); k++)
      {
        prob.x[curp] = &x_space[j];
        prob.y[curp] = m_vecInstances[i].sentenceTagIds[k];
        //fout << prob.y[curp];
        HeapFeature * heap = new HeapFeature(m_vecInstances[i].fvs[k].m_fv.size());
        
        for(curf = 0; curf < m_vecInstances[i].fvs[k].m_fv.size(); curf++)
        {
          heap->add(m_vecInstances[i].fvs[k].m_fv[curf]);          
        }

        for(curf = 0; curf < m_vecInstances[i].fvs[k].m_fv.size(); curf++)
        {
          x_space[j].index = heap->get(curf)->index+1;
          x_space[j].value = heap->get(curf)->value;
          //fout << "\t" << x_space[j].index << ":" << x_space[j].value;
          ++j;
        }
        //fout << endl;
        delete heap;

        if(prob.bias >= 0)
			    x_space[j++].value = prob.bias;
		    x_space[j++].index = -1;    
        curp++;
      }		  
	  }

    if(prob.bias >= 0)
	  {
		  prob.n=max_index+2;
		  for(i=1;i<prob.l;i++)
			  (prob.x[i]-2)->index = prob.n; 
		  x_space[j-2].index = prob.n;
	  }
	  else
		  prob.n=max_index+1;

    //fout.close();

  }


  void init_problems(const vector<JSTInstance>& m_vecInstances, int max_index, set<int> excludeIds)
  {
    int i, k,curp,curf;
	  long int elements, j;
	  char *endptr;
	  char *idx, *val, *label;
    prob.l = 0;
	  elements = 0;

    for(i=0;i<m_vecInstances.size();i++)
	  {
      if(excludeIds.find(i) != excludeIds.end())continue;
      for(k=0; k<m_vecInstances[i].sentenceTagIds.size(); k++)
      {
        prob.l++;
        elements += m_vecInstances[i].fvs[k].m_fv.size() + 1;
      }
    }

    prob.bias=bias;

    prob.y = Malloc(int,prob.l);
    prob.x = Malloc(struct feature_node *,prob.l);
    x_space = Malloc(struct feature_node,elements+prob.l);

	  j=0;
    curp=0;
    //ofstream fout("train.corpus.libsvm");
    for(i=0;i<m_vecInstances.size();i++)
	  {
      if(excludeIds.find(i) != excludeIds.end())continue;
      for(k=0; k<m_vecInstances[i].sentenceTagIds.size(); k++)
      {
        prob.x[curp] = &x_space[j];
        prob.y[curp] = m_vecInstances[i].sentenceTagIds[k];
        //fout << prob.y[curp];
        HeapFeature * heap = new HeapFeature(m_vecInstances[i].fvs[k].m_fv.size());
        
        for(curf = 0; curf < m_vecInstances[i].fvs[k].m_fv.size(); curf++)
        {
          heap->add(m_vecInstances[i].fvs[k].m_fv[curf]);          
        }

        for(curf = 0; curf < m_vecInstances[i].fvs[k].m_fv.size(); curf++)
        {
          x_space[j].index = heap->get(curf)->index+1;
          x_space[j].value = heap->get(curf)->value;
          //fout << "\t" << x_space[j].index << ":" << x_space[j].value;
          ++j;
        }
        //fout << endl;
        delete heap;

        if(prob.bias >= 0)
			    x_space[j++].value = prob.bias;
		    x_space[j++].index = -1;    
        curp++;
      }		  
	  }

    if(prob.bias >= 0)
	  {
		  prob.n=max_index+2;
		  for(i=1;i<prob.l;i++)
			  (prob.x[i]-2)->index = prob.n; 
		  x_space[j-2].index = prob.n;
	  }
	  else
		  prob.n=max_index+1;

    //fout.close();

  }

  void exit_with_help_for_predict()
  {
	  printf(
	  "Usage: predict [options] test_file model_file output_file\n"
	  "options:\n"
	  "-b probability_estimates: whether to output probability estimates, 0 or 1 (default 0)\n"
	  );
	  exit(1);
  }


  void initPredictParam(const JSTOptions &options)
  {
    nr_class=get_nr_class(model_);  
	  nr_feature=get_nr_feature(model_);
    /*
	  if(flag_predict_probability)
	  {
		  int *labels;
		  if(!check_probability_model(model_))
		  {
			  fprintf(stderr, "probability output is only supported for logistic regression\n");
			  exit(1);
		  }

		  labels=(int *) malloc(nr_class*sizeof(int));
		  get_labels(model_,labels);
		  prob_estimates = (double *) malloc(nr_class*sizeof(double));
		  fprintf(output,"labels");		
		  for(j=0;j<nr_class;j++)
			  fprintf(output," %d",labels[j]);
		  fprintf(output,"\n");
		  free(labels);
	  }
    */
  }

};



#endif