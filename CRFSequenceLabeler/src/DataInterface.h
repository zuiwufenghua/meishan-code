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
#include "Instance.h"
#include "NRMat.h"
#include "FeatureDictionary.h"


using namespace std;


class DataInterface
{
	FeatureDictionary _label_dict;
	FeatureDictionary _unifeat_dict;
	FeatureDictionary _bifeat_dict;


/*
public:
	DataInterface(void) {
		bTrain = false;
		bTest = false;
	}

  ~DataInterface(void)
  {
  }



  crfsuite_dictionary_t *tagger_attrs; 
  crfsuite_dictionary_t *tagger_labels;
  int taggerL;
  crfsuite_instance_t tagger_inst;
  crfsuite_item_t tagger_item;
  crfsuite_attribute_t tagger_cont;
  


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
  }*/
};
