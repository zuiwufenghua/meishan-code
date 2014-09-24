package is2.parser;

import is2.data.DataF;
import is2.data.FV;
import is2.data.IFV;
import is2.data.Long2IntInterface;
import is2.util.OptionsSuper;

final public class Extractor {

	public static short s_rel, s_pos, s_word, s_type, s_dir, s_dist, s_feat,
			s_child;

	final public MFO mf;
	final private Long2IntInterface li;

	final MFO.Data4 d0 = new MFO.Data4();
	final MFO.Data4 dl1 = new MFO.Data4();
	final MFO.Data4 dl2 = new MFO.Data4();
	final MFO.Data4 dr = new MFO.Data4();
	final MFO.Data4 dwr = new MFO.Data4();
	final MFO.Data4 dwwp = new MFO.Data4();

	final MFO.Data4 dw = new MFO.Data4();
	final MFO.Data4 dwp = new MFO.Data4();

	final MFO.Data4 dlf = new MFO.Data4();
	final MFO.Data4 dlf1 = new MFO.Data4();
	final MFO.Data4 dlf2 = new MFO.Data4();
	final MFO.Data4 dlcf = new MFO.Data4();
	final MFO.Data4 dlccf = new MFO.Data4();
	
	final MFO.Data4 dlsthead = new MFO.Data4();
	final MFO.Data4 dlstlabel = new MFO.Data4();
	
	final MFO.Data4 dlstheadw1 = new MFO.Data4();
	final MFO.Data4 dlstlabelw1 = new MFO.Data4();
	
	final MFO.Data4 dlstheadw2 = new MFO.Data4();
	final MFO.Data4 dlstlabelw2 = new MFO.Data4();

	public Extractor(Long2IntInterface l2i) {
		mf = new MFO();
		li = l2i;
	}

	public static void initStat() {
		MFO mf = new MFO();
		s_rel = mf.getFeatureBits(REL);
		s_pos = mf.getFeatureBits(POS);
		s_word = mf.getFeatureBits(WORD);
		s_type = mf.getFeatureBits(TYPE);
		s_dir = mf.getFeatureBits(DIR);
		la = mf.getValue(DIR, LA);
		ra = mf.getValue(DIR, RA);
		s_dist = mf.getFeatureBits(DIST);
		s_feat = mf.getFeatureBits(Pipe.FEAT);
	}

	public void init() {
		d0.a0 = s_type;
		d0.a1 = s_pos;
		d0.a2 = s_pos;
		d0.a3 = s_pos;
		d0.a4 = s_pos;
		d0.a5 = s_pos;
		d0.a6 = s_pos;
		d0.a7 = s_pos;
		dl1.a0 = s_type;
		dl1.a1 = s_rel;
		dl1.a2 = s_pos;
		dl1.a3 = s_pos;
		dl1.a4 = s_pos;
		dl1.a5 = s_pos;
		dl1.a6 = s_pos;
		dl1.a7 = s_pos;
		dl2.a0 = s_type;
		dl2.a1 = s_rel;
		dl2.a2 = s_word;
		dl2.a3 = s_word;
		dl2.a4 = s_pos;
		dl2.a5 = s_pos;
		dl2.a6 = s_pos;
		dl2.a7 = s_pos;
		dwp.a0 = s_type;
		dwp.a1 = s_rel;
		dwp.a2 = s_word;
		dwp.a3 = s_pos;
		dwp.a4 = s_pos;
		dwp.a5 = s_word;
		dwwp.a0 = s_type;
		dwwp.a1 = s_rel;
		dwwp.a2 = s_word;
		dwwp.a3 = s_word;
		dwwp.a4 = s_pos;
		dwwp.a5 = s_word;
		dlf.a0 = s_type;
		dlf.a1 = s_rel;
		dlf.a2 = s_pos;
		dlf.a3 = s_pos;
		dlf.a4 = s_feat;
		dlf.a5 = s_feat;
		dlf.a6 = s_pos;
		dlf.a7 = s_pos;
		dlf1.a0 = s_type;
		dlf1.a1 = s_rel;
		dlf1.a2 = s_pos;
		dlf1.a3 = s_feat;
		dlf1.a4 = s_pos;
		dlf1.a5 = s_pos;
		dlf2.a0 = s_type;
		dlf2.a1 = s_rel;
		dlf2.a2 = s_pos;
		dlf2.a3 = s_feat;
		dlf2.a4 = s_feat;
		dlf2.a5 = s_pos;
		dlf2.a6 = s_pos;
		dlf2.a7 = s_pos;
		dlcf.a0 = s_type;
		dlcf.a1 = s_rel;
		dlcf.a2 = s_feat;
		dlcf.a3 = s_pos;
		dlcf.a4 = s_pos;
		dlcf.a5 = s_pos;
		dlccf.a0 = s_type;
		dlccf.a1 = s_rel;
		dlccf.a2 = s_feat;
		dlccf.a3 = s_feat;
		dlccf.a4 = s_pos;
		dlccf.a5 = s_pos;
		dlccf.a6 = s_pos;
		
		dlsthead.a0 = s_type;
		dlsthead.a1 = s_rel;
		dlsthead.a2 = s_pos;
		dlsthead.a3 = s_pos;
		dlsthead.a4 = s_pos;
		dlsthead.a5 = s_pos;
		dlsthead.a6 = s_pos;
		dlsthead.a7 = s_pos;
		
		dlstlabel.a0 = s_type;
		dlstlabel.a1 = s_rel;
		dlstlabel.a2 = s_rel;
		dlstlabel.a3 = s_pos;
		dlstlabel.a4 = s_pos;
		dlstlabel.a5 = s_pos;
		dlstlabel.a6 = s_pos;
		dlstlabel.a7 = s_pos;
		
		
		dlstheadw1.a0 = s_type;
		dlstheadw1.a1 = s_rel;
		dlstheadw1.a2 = s_word;
		dlstheadw1.a3 = s_pos;
		dlstheadw1.a4 = s_pos;
		dlstheadw1.a5 = s_pos;
		dlstheadw1.a6 = s_pos;
		dlstheadw1.a7 = s_pos;
		
		dlstlabelw1.a0 = s_type;
		dlstlabelw1.a1 = s_rel;
		dlstlabelw1.a2 = s_rel;
		dlstlabelw1.a3 = s_word;
		dlstlabelw1.a4 = s_pos;
		dlstlabelw1.a5 = s_pos;
		dlstlabelw1.a6 = s_pos;
		dlstlabelw1.a7 = s_pos;
		
		dlstheadw2.a0 = s_type;
		dlstheadw2.a1 = s_rel;
		dlstheadw2.a2 = s_word;
		dlstheadw2.a3 = s_word;
		dlstheadw2.a4 = s_pos;
		dlstheadw2.a5 = s_pos;
		dlstheadw2.a6 = s_pos;
		dlstheadw2.a7 = s_pos;
		
		dlstlabelw2.a0 = s_type;
		dlstlabelw2.a1 = s_rel;
		dlstlabelw2.a2 = s_rel;
		dlstlabelw2.a3 = s_word;
		dlstlabelw2.a4 = s_word;
		dlstlabelw2.a5 = s_pos;
		dlstlabelw2.a6 = s_pos;
		dlstlabelw2.a7 = s_pos;
	}

	public void basic(short[] pposs, int p, int d, IFV f) {

		int dir = (p < d) ? ra : la;
		d0.v0 = _f39;
		d0.v1 = pposs[p];
		d0.v2 = pposs[d]; // d0.stop=4;
		int end = (p >= d ? p : d);
		int start = (p >= d ? d : p) + 1;

		for (int i = start; i < end; i++) {
			d0.v3 = pposs[i];
			long l = mf.calc4(d0);
			f.add(li.l2i(l = d0.calcs(s_dir, dir, l)));
		}

	}

	public void first(short[] pposs, int[] form, int[] lemmas, int[][] feats,
			short[] phead, short[] prel, int prnt, int dpnt, int label, IFV f) {
		int prntF = form[prnt];
		int dpntF = form[dpnt];

		int prntL = lemmas[prnt];
		int dpntL = lemmas[dpnt];

		int prntP = pposs[prnt];
		int dpntP = pposs[dpnt];

		final int dir = (prnt < dpnt) ? ra : la;

		long l;


		dl2.v0 = _f1;
		dl2.v1 = label;
		dl2.v2 = prntF;
		dl2.v3 = prntP;
		l = mf.calc4(dl2);
		l = dl2.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl2.v0 = _f2;
		l = mf.calc3(dl2);
		l = dl2.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl2.v0 = _f4;
		dl2.v2 = dpntF;
		dl2.v3 = dpntP;
		l = mf.calc4(dl2);
		l = dl2.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl2.v0 = _f5;
		l = mf.calc3(dl2);
		l = dl2.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v1 = label;
		dl1.v0 = _f6;
		dl1.v2 = dpntP;
		l = mf.calc3(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl1.v0 = _f3;
		dl1.v2 = prntP;
		l = mf.calc3(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl1.v0 = _f13; /* dl1.v2=prntP; */
		dl1.v3 = dpntP;
		l = mf.calc4(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dwp.v1 = label;
		dwp.v0 = _f7;
		dwp.v2 = prntF;
		dwp.v3 = prntP;
		dwp.v4 = dpntP;
		dwp.v5 = dpntF;
		l = mf.calc6(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dwp.v0 = _f10; /* dwp.v2=prntF; dwp.v3=prntP; dwp.v4=dpntP; */
		l = mf.calc5(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dwp.v0 = _f8;
		dwp.v2 = dpntF; /* dwp.v3=prntP;dwp.v[4]=dpntP; */
		l = mf.calc5(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dwwp.v0 = _f9;
		dwwp.v1 = label;
		dwwp.v2 = prntF;
		dwwp.v3 = dpntF;
		dwwp.v4 = dpntP;
		l = mf.calc5(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dwwp.v0 = _f11;
		dwwp.v2 = prntF;
		dwwp.v3 = dpntF;
		dwwp.v4 = prntP;
		l = mf.calc5(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dwwp.v0 = _f12;
		dwwp.v2 = prntF;
		dwwp.v3 = dpntF;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		// lemmas

		dl2.v0 = _f1l;
		dl2.v2 = prntL;
		dl2.v3 = prntP;
		l = mf.calc4(dl2);
		l = dl2.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl2.v0 = _f2l; /* dl2.v2=prntL; */
		l = mf.calc3(dl2);
		l = dl2.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl2.v0 = _f4l;
		dl2.v2 = dpntL;
		dl2.v3 = dpntP;
		l = mf.calc4(dl2);
		l = dl2.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl2.v0 = _f5l; /* dl2.v2=dpntL; */
		l = mf.calc3(dl2);
		l = dl2.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f6l;
		dl1.v2 = dpntP;
		l = mf.calc3(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f3l; /* d3l1.v1=label; */
		dl1.v2 = prntP;
		l = mf.calc3(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		// Please try to remove this feature since it doubles _f13
		dl1.v0 = _f13l;
		dl1.v2 = prntP;
		dl1.v3 = dpntP;
		l = mf.calc4(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dwp.v0 = _f7l; /* dwp.v1=label; */
		dwp.v2 = dpntL;
		dwp.v3 = prntP;
		dwp.v4 = dpntP;
		dwp.v5 = prntL;
		l = mf.calc6(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dwp.v0 = _f8l;
		l = mf.calc5(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dwp.v0 = _f10l; /* dwp.v1=label; */
		dwp.v2 = prntL;
		l = mf.calc5(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dwwp.v0 = _f9l; /* dwwp.v1=label; */
		dwwp.v2 = prntL;
		dwwp.v3 = dpntL;
		dwwp.v4 = dpntP;
		l = mf.calc5(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dwwp.v0 = _f11l; /* dwwp.v1=label; */
		dwwp.v4 = prntP;
		l = mf.calc5(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dwwp.v0 = _f12l; /* dwwp.v1=label; */
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		// linear features
		int parentm1 = prnt - 1;
		int parentp1 = prnt + 1;

		int depm1 = dpnt - 1;
		int depp1 = dpnt + 1;

		int prntPm1 = parentm1 >= 0 ? pposs[parentm1] : s_str;
		int chldPm1 = depm1 >= 0 ? pposs[depm1] : s_str;
		int prntPp1 = parentp1 <= pposs.length - 1 ? pposs[parentp1] : s_end;
		int chldPp1 = depp1 <= pposs.length - 1 ? pposs[depp1] : s_end;

		dl1.v0 = _f14; /* d3l1.v1=label; */
		dl1.v2 = prntP;
		dl1.v3 = prntPp1;
		dl1.v4 = dpntP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f15; /* d3l1.v1=label; */
		dl1.v3 = chldPm1;
		dl1.v4 = dpntP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f16; /* d3l1.v1=label; */
		dl1.v3 = dpntP;
		dl1.v4 = chldPp1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f17;
		dl1.v3 = prntPp1;
		dl1.v4 = chldPm1;
		dl1.v5 = dpntP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f18;
		dl1.v2 = prntPm1;
		dl1.v3 = prntP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f19; /* d3l1.v1=label; */
		dl1.v2 = prntP;
		dl1.v3 = prntPp1;
		dl1.v4 = dpntP;
		dl1.v5 = chldPp1;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f20; /* d3l1.v1=label; */
		dl1.v2 = prntPm1;
		dl1.v3 = prntP;
		dl1.v4 = dpntP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		
		if (_options.usedep) {
			int itype = 0;
			if (prnt == phead[dpnt]) {
				itype = 1;
			} else if (dpnt == phead[prnt]) {
				itype = 2;
			} else if (phead[prnt] == phead[dpnt]) {
				itype = 3;
			} else if ((phead[prnt] >= 0) && (phead[prnt] < phead.length)
					&& (phead[phead[prnt]] == dpnt)) {
				itype = 4;
			} else if ((phead[dpnt] >= 0) && (phead[dpnt] < phead.length)
					&& (phead[phead[dpnt]] == prnt)) {
				itype = 5;
			} else {
				itype = 6;
			}

			/*
			dlsthead.v1 = label;
			dlsthead.v0 = _f440;
			dlsthead.v2 = prntP;
			dlsthead.v3 = dpntP;
			dlsthead.v4 = itype;
			l = mf.calc5(dlsthead);
			l = dlsthead.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			*/
			dlstlabel.v1 = label;
			dlstlabel.v0 = _f441;
			dlstlabel.v2 = prel[dpnt];
			dlstlabel.v3 = prntP;
			dlstlabel.v4 = dpntP;
			dlstlabel.v5 = itype;
			l = mf.calc6(dlstlabel);
			l = dlstlabel.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dlstlabelw1.v1 = label;
			dlstlabelw1.v0 = _f442;
			dlstlabelw1.v2 = prel[dpnt];
			dlstlabelw1.v3 = prntF;
			dlstlabelw1.v4 = dpntP;
			dlstlabelw1.v5 = itype;
			l = mf.calc6(dlstlabelw1);
			l = dlstlabelw1.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dlstlabelw1.v1 = label;
			dlstlabelw1.v0 = _f443;
			dlstlabelw1.v2 = prel[dpnt];
			dlstlabelw1.v3 = dpntF;
			dlstlabelw1.v4 = prntP;
			dlstlabelw1.v5 = itype;
			l = mf.calc6(dlstlabelw1);
			l = dlstlabelw1.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dlstlabelw2.v1 = label;
			dlstlabelw2.v0 = _f444;
			dlstlabelw2.v2 = prel[dpnt];
			dlstlabelw2.v3 = dpntF;
			dlstlabelw2.v4 = prntF;
			dlstlabelw2.v5 = itype;
			l = mf.calc6(dlstlabelw2);
			l = dlstlabelw2.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			
		}

		 
		
		/*
		if (_options.usedep && prnt == phead[dpnt]) {
			int itype = 1;
			if(prnt == phead[dpnt])
			{
				itype = 2;
			}
			dl2.v1 = itype;
			dl2.v0 = _f440;
			dl2.v2 = prntP;
			dl2.v3 = dpntP;
			l = mf.calc4(dl2);
			l = dl2.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dl2.v1 = itype;
			dl2.v0 = _f441;
			dl2.v2 = prntF;
			dl2.v3 = dpntF;
			l = mf.calc4(dl2);
			l = dl2.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dl2.v1 = itype;
			dl2.v0 = _f442;
			dl2.v2 = prntF;
			dl2.v3 = dpntP;
			l = mf.calc4(dl2);
			l = dl2.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dl2.v1 = itype;
			dl2.v0 = _f443;
			dl2.v2 = prntP;
			dl2.v3 = dpntF;
			l = mf.calc4(dl2);
			l = dl2.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
		}
		*/
		if (feats == null)
			return;

		// if (_f41>0) {
		// short[] featsP =feats[prnt], featsD =feats[dpnt];
		// if(featsD == null || featsD.length != 2)return;
		// dlf.v0= _f41; dlf.v1=label; dlf.v2=prntP; dlf.v3=dpntP;
		// dlf.v0= _f41; dlf.v1=nofeat; dlf.v2=nofeat; dlf.v3=nofeat;
		// extractFeat(f, dir, featsP, featsD);
		// for stacklearning only

		/*
		 * if(featsD != null && featsD.length == 2 && featsD[0] == prnt) {
		 * dlf.v0= _f41; dlf.v1=label; //dlf.v2=nofeat; dlf.v3=nofeat; dlf.v2=1;
		 * dlf.v3=featsD[1]; l= mf.calc4(dlf); l=dlf.calcs(s_dir,dir,l);
		 * f.add(li.l2i(l));
		 * 
		 * dlf.v0= _f4l1; dlf.v1=label; dlf.v2=prntP; dlf.v3=dpntP; dlf.v4=1;
		 * dlf.v5=featsD[1]; l= mf.calc6(dlf); l=dlf.calcs(s_dir,dir,l);
		 * f.add(li.l2i(l)); }
		 */
		// cfgfeatures
		if (_options.usecfg) {
			int smaller = prnt < dpnt ? prnt : dpnt;
			int bigger = prnt < dpnt ? dpnt : prnt;
			int[] featsSmall = feats[smaller];
			int sentenceSize = pposs.length;
			int featSize = (sentenceSize - smaller)
					* (sentenceSize - smaller - 1);
			// int featSize = (sentenceSize-smaller-1);
			if (featSize > 0 && featsSmall != null
					&& featsSmall.length == featSize) {
				int featSeq = bigger - smaller - 1;
				dlcf.v0 = _f4l0;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq];
				l = mf.calc3(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlcf.v0 = _f4l1;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq];
				dlcf.v3 = prntP;
				dlcf.v4 = dpntP;
				l = mf.calc5(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlcf.v0 = _f422;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq + 1];
				l = mf.calc3(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlcf.v0 = _f423;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq + 1];
				dlcf.v3 = prntP;
				dlcf.v4 = dpntP;
				l = mf.calc5(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlccf.v0 = _f424;
				dlccf.v1 = label;
				dlccf.v2 = featsSmall[2 * featSeq];
				dlccf.v3 = featsSmall[2 * featSeq + 1];
				l = mf.calc4(dlccf);
				l = dlccf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlccf.v0 = _f425;
				dlccf.v1 = label;
				dlccf.v2 = featsSmall[2 * featSeq];
				dlccf.v3 = featsSmall[2 * featSeq + 1];
				dlccf.v4 = prntP;
				dlccf.v5 = dpntP;
				l = mf.calc6(dlccf);
				l = dlccf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));
			}
		}
		// depfeatures
		/*
		 * if(_options.usedep) { short[] featsD =feats[dpnt]; int sentenceSize =
		 * pposs.length; int featSize = (sentenceSize-dpnt) *
		 * (sentenceSize-dpnt-1); //int featSize = (sentenceSize-dpnt-1);
		 * if(featSize > 0 && featsD != null && featsD.length == featSize + 2 )
		 * { int s1 = (featsD[featSize] == prnt)? 1: 0; dlf1.v0= _f4l2;
		 * dlf1.v1=label; dlf1.v2=s1; dlf1.v3=featsD[featSize+1]; l=
		 * mf.calc4(dlf1); l=dlf1.calcs(s_dir,dir,l); f.add(li.l2i(l));
		 * 
		 * dlf1.v0= _f4l2; dlf1.v1=label; dlf1.v2=s1;
		 * dlf1.v3=featsD[featSize+1]; dlf1.v4=prntP; dlf1.v5=dpntP; l=
		 * mf.calc6(dlf1); l=dlf1.calcs(s_dir,dir,l); f.add(li.l2i(l)); } }
		 */
		// }
	}

	/**
	 * Extract grandchild features
	 * 
	 * @param pos
	 * @param forms
	 * @param lemmas
	 * @param feats
	 * @param prnt
	 *            parent
	 * @param chld
	 *            child
	 * @param gc
	 *            grandchild
	 * @param label
	 * @param f
	 *            feature collector or summerrizer
	 */
	public void grandchildren(short[] pos, int[] forms, int[] lemmas,
			int[][] feats, short[] phead, short[] prel, int prnt, int chld,
			int gc, int label, IFV f) {

		int prntP = pos[prnt], chldP = pos[chld];
		int prntF = forms[prnt], chldF = forms[chld];
		int prntL = lemmas[prnt], chldL = lemmas[chld];

		int gcP = gc != -1 ? pos[gc] : s_str;
		int gcF = gc != -1 ? forms[gc] : s_stwrd;
		int gcL = gc != -1 ? lemmas[gc] : s_stwrd;

		long l;

		int dir = (prnt < chld) ? ra : la, dir_gra = (chld < gc) ? ra : la;

		dl1.v1 = label;
		dl1.v0 = _f21;
		dl1.v2 = prntP;
		dl1.v3 = chldP;
		dl1.v4 = gcP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		l = dl1.calcs(s_dir, dir_gra, l);
		f.add(li.l2i(l));

		dl1.v0 = _f22;
		dl1.v2 = prntP;
		dl1.v3 = gcP;
		l = mf.calc4(dl1);
		l = dl1.calcs(s_dir, dir, l);
		l = dl1.calcs(s_dir, dir_gra, l);
		f.add(li.l2i(l));

		dl1.v0 = _f23;
		dl1.v2 = chldP;
		dl1.v3 = gcP;
		l = mf.calc4(dl1);
		l = dl1.calcs(s_dir, dir, l);
		l = dl1.calcs(s_dir, dir_gra, l);
		f.add(li.l2i(l));

		dwwp.v1 = label;
		dwwp.v0 = _f24;
		dwwp.v2 = prntF;
		dwwp.v3 = gcF;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		l = dwwp.calcs(s_dir, dir_gra, l);
		f.add(li.l2i(l));

		dwwp.v0 = _f25;
		dwwp.v2 = chldF;
		dwwp.v3 = gcF;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwwp.calcs(s_dir, dir_gra, l)));

		dwp.v1 = label;
		dwp.v0 = _f26;
		dwp.v2 = gcF;
		dwp.v3 = prntP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwp.calcs(s_dir, dir_gra, l)));

		dwp.v0 = _f27;
		dwp.v2 = gcF;
		dwp.v3 = chldP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwp.calcs(s_dir, dir_gra, l)));

		dwp.v0 = _f28;
		dwp.v2 = prntF;
		dwp.v3 = gcP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwp.calcs(s_dir, dir_gra, l)));

		dwp.v0 = _f29;
		dwp.v2 = chldF;
		dwp.v3 = gcP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwp.calcs(s_dir, dir_gra, l)));

		// lemma

		dwwp.v0 = _f24l;
		dwwp.v2 = prntL;
		dwwp.v3 = gcL;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		l = dwwp.calcs(s_dir, dir_gra, l);
		f.add(li.l2i(l));

		dwwp.v0 = _f25l;
		dwwp.v2 = chldL;
		dwwp.v3 = gcL;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwwp.calcs(s_dir, dir_gra, l)));

		dwp.v0 = _f26l;
		dwp.v2 = gcL;
		dwp.v3 = prntP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwp.calcs(s_dir, dir_gra, l)));

		dwp.v0 = _f27l;
		dwp.v2 = gcL;
		dwp.v3 = chldP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwp.calcs(s_dir, dir_gra, l)));

		dwp.v0 = _f28l;
		dwp.v2 = prntL;
		dwp.v3 = gcP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwp.calcs(s_dir, dir_gra, l)));

		dwp.v0 = _f29l;
		dwp.v2 = chldL;
		dwp.v3 = gcP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l = dwp.calcs(s_dir, dir_gra, l)));

		// linear features

		int prntPm1 = prnt != 0 ? pos[prnt - 1] : s_str; // parent-pos-minus1
		int chldPm1 = chld - 1 >= 0 ? pos[chld - 1] : s_str; // child-pos-minus1
		int prntPp1 = prnt != pos.length - 1 ? pos[prnt + 1] : s_end;
		int chldPp1 = chld != pos.length - 1 ? pos[chld + 1] : s_end;

		int gcPm1 = gc > 0 ? pos[gc - 1] : s_str;
		int gcPp1 = gc < pos.length - 1 ? pos[gc + 1] : s_end;

		dl1.v0 = _f42;
		dl1.v2 = gcP;
		dl1.v3 = gcPp1;
		dl1.v4 = chldP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f43;
		dl1.v2 = gcP;
		dl1.v3 = gcPm1;
		dl1.v4 = chldP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f44;
		dl1.v2 = gcP;
		dl1.v3 = chldP;
		dl1.v4 = chldPp1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f45;
		dl1.v2 = gcP;
		dl1.v3 = chldP;
		dl1.v4 = chldPm1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f46;
		dl1.v2 = gcP;
		dl1.v3 = gcPp1;
		dl1.v4 = chldPm1;
		dl1.v5 = chldP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f47;
		dl1.v2 = gcPm1;
		dl1.v3 = gcP;
		dl1.v4 = chldPm1;
		dl1.v5 = chldP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f48;
		dl1.v2 = gcP;
		dl1.v3 = gcPp1;
		dl1.v4 = chldP;
		dl1.v5 = chldPp1;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f49;
		dl1.v2 = gcPm1;
		dl1.v3 = gcP;
		dl1.v4 = chldP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f50;
		dl1.v2 = gcP;
		dl1.v3 = gcPp1;
		dl1.v4 = prntP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f51;
		dl1.v2 = gcP;
		dl1.v3 = gcPm1;
		dl1.v4 = prntP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f52;
		dl1.v2 = gcP;
		dl1.v3 = prntP;
		dl1.v4 = prntPp1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f53;
		dl1.v2 = gcP;
		dl1.v3 = prntP;
		dl1.v4 = prntPm1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f54;
		dl1.v2 = gcP;
		dl1.v3 = gcPp1;
		dl1.v4 = prntPm1;
		dl1.v5 = prntP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		// I think that I introduced a bug:
		// dl1.v0= _f55; dl1.v2=gcPm1; dl1.v3=gcP;//dl1.v4=chldPm1;dl1.v5=cldP;
		dl1.v0 = _f55;
		dl1.v2 = gcPm1;
		dl1.v3 = gcP;
		dl1.v4 = prntPm1;
		dl1.v5 = prntP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f56;
		dl1.v2 = gcP;
		dl1.v3 = gcPp1;
		dl1.v4 = prntP;
		dl1.v5 = prntPp1;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		// I think that I introduced a bug:
		// dl1.v0= _f57; dl1.v2=gcPm1; dl1.v3=gcP; dl1.v4=prntP;
		// //dl1.v5=chldPp1;
		dl1.v0 = _f57;
		dl1.v2 = gcPm1;
		dl1.v3 = gcP;
		dl1.v4 = prntP;
		dl1.v5 = prntPp1;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		
		if (_options.usedep) {
			int itype = 0;
			if (gc == -1) {
				if (prnt == phead[chld]) {
					itype = 1;
				} else if (chld == phead[prnt]) {
					itype = 2;
				} else if (phead[prnt] == phead[chld]) {
					itype = 3;
				} else if ((phead[prnt] >= 0) && (phead[prnt] < phead.length)
						&& (phead[phead[prnt]] == chld)) {
					itype = 4;
				} else if ((phead[chld] >= 0) && (phead[chld] < phead.length)
						&& (phead[phead[chld]] == prnt)) {
					itype = 5;
				} else {
					itype = 6;
				}
			} else {
				if (prnt == phead[chld] && chld == phead[gc]) {
					itype = 7;
				} else if (chld == phead[prnt] && chld == phead[gc]) {
					itype = 8;
				} else if (prnt == phead[chld] && prnt == phead[gc]) {
					itype = 9;
				} else if (prnt == phead[gc] && gc == phead[chld]) {
					itype = 10;
				} else if (phead[gc] == phead[chld] && phead[chld] > 0
						&& phead[chld] < phead.length
						&& phead[phead[chld]] == prnt) {
					itype = 11;
				} else if (phead[gc] == chld && phead[chld] > 0
						&& phead[chld] < phead.length
						&& phead[phead[chld]] == prnt) {
					itype = 12;
				} else if (phead[chld] == prnt && phead[gc] > 0
						&& phead[gc] < phead.length && phead[phead[gc]] == chld) {
					itype = 13;
				} else {
					itype = 14;
				}

			}
			
			
			dlstlabel.v1 = label;
			dlstlabel.v0 = _f451;
			dlstlabel.v2 = prel[chld];
			dlstlabel.v3 = prntP;
			dlstlabel.v4 = gcP;
			dlstlabel.v5 = itype;
			l = mf.calc6(dlstlabel);
			l = dlstlabel.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
						
			dlstlabel.v1 = label;
			dlstlabel.v0 = _f452;
			dlstlabel.v2 = prel[chld];
			dlstlabel.v3 = prntP;
			dlstlabel.v4 = chldP;
			dlstlabel.v5 = gcP;
			dlstlabel.v6 = itype;
			l = mf.calc7(dlstlabel);
			l = dlstlabel.calcs(s_dir, dir, l);
			f.add(li.l2i(l));	
			
			dlstlabelw1.v1 = label;
			dlstlabelw1.v0 = _f453;
			dlstlabelw1.v2 = prel[chld];
			dlstlabelw1.v3 = prntF;
			dlstlabelw1.v4 = chldP;
			dlstlabelw1.v5 = gcP;
			dlstlabelw1.v6 = itype;
			l = mf.calc7(dlstlabelw1);
			l = dlstlabelw1.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dlstlabelw1.v1 = label;
			dlstlabelw1.v0 = _f454;
			dlstlabelw1.v2 = prel[chld];
			dlstlabelw1.v3 = chldF;
			dlstlabelw1.v4 = prntP;
			dlstlabelw1.v5 = gcP;
			dlstlabelw1.v6 = itype;
			l = mf.calc7(dlstlabelw1);
			l = dlstlabelw1.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dlstlabelw1.v1 = label;
			dlstlabelw1.v0 = _f455;
			dlstlabelw1.v2 = prel[chld];
			dlstlabelw1.v3 = gcF;
			dlstlabelw1.v4 = prntP;
			dlstlabelw1.v5 = chldP;
			dlstlabelw1.v6 = itype;
			l = mf.calc7(dlstlabelw1);
			l = dlstlabelw1.calcs(s_dir, dir, l);
			f.add(li.l2i(l));

		}
		
		
		if (feats == null)
			return;

		// short[] featsP =feats[chld];
		// short[] featsD =gc!=-1?feats[gc]:null;
		/*
		 * dlf.v0= _f74; dlf.v1=label; dlf.v2=gcP; dlf.v3=chldP; extractFeat(f,
		 * dir, featsP, featsD);
		 */

		/*
		 * short[] featsP =feats[prnt], featsD =feats[chld], featsGC
		 * =gc!=-1?feats[gc]:null; //if(featsD == null || featsD.length !=
		 * 2)return; //dlf.v0= _f41; dlf.v1=label; dlf.v2=prntP; dlf.v3=dpntP;
		 * //dlf.v0= _f41; dlf.v1=nofeat; dlf.v2=nofeat; dlf.v3=nofeat;
		 * //extractFeat(f, dir, featsP, featsD); //for stacklearning only
		 * 
		 * if(featsD != null && featsGC != null && featsD.length == 2 &&
		 * featsGC.length == 2 && featsD[0] == prnt && featsGC[0] == chld) {
		 * dlf.v0= _f4l2; dlf.v1=label; //dlf.v2=nofeat; dlf.v3=nofeat;
		 * dlf.v2=1; dlf.v3=featsD[1]; l= mf.calc4(dlf);
		 * l=dlf.calcs(s_dir,dir,l); f.add(li.l2i(l));
		 * 
		 * dlf.v0= _f4l3; dlf.v1=label; dlf.v2=prntP; dlf.v3=chldP;
		 * dlf.v4=gcP;dlf.v5=1; dlf.v6=featsD[1]; l= mf.calc7(dlf);
		 * l=dlf.calcs(s_dir,dir,l); f.add(li.l2i(l)); }
		 */

		if (_options.usecfg) {
			if (gc == -1)
				return;
			int smaller = prnt < chld ? prnt : chld;
			int bigger = prnt < chld ? chld : prnt;
			int midder = -1;

			if (gc < smaller) {
				midder = smaller;
				smaller = gc;
			} else if (gc > smaller && gc < bigger) {
				midder = gc;
			} else {
				midder = bigger;
				bigger = gc;
			}

			int[] featsSmall = feats[smaller];
			int sentenceSize = forms.length;
			int featSize = (sentenceSize - smaller)
					* (sentenceSize - smaller - 1);
			if (featSize > 0 && featsSmall != null
					&& featsSmall.length == featSize) {
				int featSeq = (midder - smaller)
						* (2 * sentenceSize - midder - smaller - 1) / 2
						+ bigger - midder - 1;

				dlcf.v0 = _f4l4;
				dlcf.v1 = label; // dlcf.v2=nofeat; dlcf.v3=nofeat;
				dlcf.v2 = featsSmall[2 * featSeq];
				l = mf.calc3(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				// l=dl1.calcs(s_dir,dir_gra,l);
				f.add(li.l2i(l));

				dlcf.v0 = _f4l5;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq];
				dlcf.v3 = prntP;
				dlcf.v4 = chldP;
				dlcf.v5 = gcP;

				l = mf.calc6(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				// l=dl1.calcs(s_dir,dir_gra,l);
				f.add(li.l2i(l));

				dlcf.v0 = _f426;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq + 1];
				l = mf.calc3(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlcf.v0 = _f427;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq + 1];
				dlcf.v3 = prntP;
				dlcf.v4 = chldP;
				dlcf.v5 = gcP;
				l = mf.calc6(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlccf.v0 = _f428;
				dlccf.v1 = label;
				dlccf.v2 = featsSmall[2 * featSeq];
				dlccf.v3 = featsSmall[2 * featSeq + 1];
				l = mf.calc4(dlccf);
				l = dlccf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlccf.v0 = _f429;
				dlccf.v1 = label;
				dlccf.v2 = featsSmall[2 * featSeq];
				dlccf.v3 = featsSmall[2 * featSeq + 1];
				dlccf.v4 = prntP;
				dlccf.v5 = chldP;
				dlccf.v6 = gcP;
				l = mf.calc7(dlccf);
				l = dlccf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

			}
		}

		/*
		 * if(_options.usedep) { int[] featsP =feats[prnt], featsD =feats[chld],
		 * featsGC =gc!=-1?feats[gc]:null;
		 * 
		 * //for stacklearning only int sentenceSize = forms.length; int
		 * featsDSize = (sentenceSize-chld) * (sentenceSize-chld-1); int
		 * featsGCSize = (sentenceSize-gc) * (sentenceSize-gc-1); //int
		 * featsDSize = (sentenceSize-chld-1); //int featsGCSize =
		 * (sentenceSize-gc-1);
		 * 
		 * if(featsD != null && featsGC != null && featsD.length == featsDSize+2
		 * && featsGC.length == featsGCSize+2) { int s1 = (featsD[featsDSize] ==
		 * prnt)? 1:0; int s2 = (featsGC[featsGCSize] == chld)? 1:0; dlf2.v0=
		 * _f4l6; dlf2.v1=label; dlf2.v2=s1*2+s1; dlf2.v3=featsD[featsDSize+1];
		 * dlf2.v4=featsGC[featsGCSize+1]; l= mf.calc3(dlf2);
		 * l=dlf2.calcs(s_dir,dir,l); //l=dl1.calcs(s_dir,dir_gra,l);
		 * f.add(li.l2i(l));
		 * 
		 * dlf2.v0= _f4l6; dlf2.v1=label; dlf2.v2=s1*2+s1;
		 * dlf2.v3=featsD[featsDSize+1]; dlf2.v4=featsGC[featsGCSize+1];
		 * dlf2.v5=prntP; dlf2.v6=chldP;dlf2.v7=gcP; l= mf.calc8(dlf2);
		 * l=dlf2.calcs(s_dir,dir,l); //l=dl1.calcs(s_dir,dir_gra,l);
		 * f.add(li.l2i(l)); } }
		 */
	}

	public void sibling(short pos[], int forms[], int[] lemmas, int[][] feats,
			short[] phead, short[] prel, int prnt, int chld, int sblng,
			int label, IFV f) {
		int prntP = pos[prnt], chldP = pos[chld];
		int prntW = forms[prnt], chldW = forms[chld];
		int prntL = lemmas[prnt], chldL = lemmas[chld];

		int sblP = sblng != -1 ? pos[sblng] : s_str, sblW = sblng != -1 ? forms[sblng]
				: s_stwrd, sblL = sblng != -1 ? lemmas[sblng] : s_stwrd;

		int dir = (prnt < chld) ? ra : la;

		int abs = Math.abs(prnt - chld);

		final int dist;
		if (abs > 10)
			dist = d10;
		else if (abs > 5)
			dist = d5;
		else if (abs == 5)
			dist = d4;
		else if (abs == 4)
			dist = d3;
		else if (abs == 3)
			dist = d2;
		else if (abs == 2)
			dist = d1;
		else
			dist = di0;

		long l;

		dl1.v0 = _f30;
		dl1.v1 = label;
		dl1.v2 = prntP;
		dl1.v3 = chldP;
		dl1.v4 = sblP; // d3l1.stop=5;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dl1.calcs(s_dist, dist, l)));

		dl1.v0 = _f31; /* dl1.v1=label; */
		dl1.v3 = sblP; // d3l1.stop=4;
		l = mf.calc4(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dl1.calcs(s_dist, dist, l)));

		dl1.v0 = _f32; /* dl1.v1=label; */
		dl1.v2 = chldP;
		l = mf.calc4(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dl1.calcs(s_dist, dist, l)));

		// sibling only could be tried

		dwwp.v0 = _f33;
		dwwp.v1 = label;
		dwwp.v2 = prntW;
		dwwp.v3 = sblW;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwwp.calcs(s_dist, dist, l)));

		dwwp.v0 = _f34; /* dwwp.v1=label; */
		dwwp.v2 = chldW;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwwp.calcs(s_dist, dist, l)));

		dwp.v0 = _f35;
		dwp.v1 = label;
		dwp.v2 = sblW;
		dwp.v3 = prntP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwp.calcs(s_dist, dist, l)));

		dwp.v0 = _f36; /* dwp.v1=label; */
		dwp.v3 = chldP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwp.calcs(s_dist, dist, l)));

		dwp.v0 = _f37; /* dwp.v1=label; */
		dwp.v2 = prntW;
		dwp.v3 = sblP;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwp.calcs(s_dist, dist, l)));

		dwp.v0 = _f38; /* dwp.v1=label; */
		dwp.v2 = chldW;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwp.calcs(s_dist, dist, l)));

		// sibling word only could be tried

		// lemmas
		dwwp.v1 = label;

		// 97
		dwwp.v0 = _f33l;
		dwwp.v2 = prntL;
		dwwp.v3 = sblL;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		// 98
		dwwp.v0 = _f34l;
		dwwp.v2 = chldL;
		dwwp.v3 = sblL;
		l = mf.calc4(dwwp);
		l = dwwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwwp.calcs(s_dist, dist, l)));

		// 99
		dwp.v0 = _f35l; /* dwp.v1=label; */
		dwp.v2 = sblL;
		dwp.v3 = prntP; // dwp.stop=4;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwp.calcs(s_dist, dist, l)));
		// 100
		dwp.v0 = _f36l; /* dwp.v1=label; */
		dwp.v3 = chldP; // dwp.stop=4;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwp.calcs(s_dist, dist, l)));

		// 101
		dwp.v0 = _f37l; /* dwp.v1=label; */
		dwp.v2 = prntL;
		dwp.v3 = sblP; // dwp.stop=4;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwp.calcs(s_dist, dist, l)));

		// 102
		dwp.v0 = _f38l; /* dwp.v1=label; */
		dwp.v2 = chldL;
		l = mf.calc4(dwp);
		l = dwp.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		f.add(li.l2i(dwp.calcs(s_dist, dist, l)));

		int prntPm1 = prnt != 0 ? pos[prnt - 1] : s_str;
		int chldPm1 = chld - 1 >= 0 ? pos[chld - 1] : s_str;
		int prntPp1 = prnt != pos.length - 1 ? pos[prnt + 1] : s_end;
		int chldPp1 = chld != pos.length - 1 ? pos[chld + 1] : s_end;

		// sibling part of speech minus and plus 1
		int sblPm1 = sblng > 0 ? pos[sblng - 1] : s_str;
		int sblPp1 = sblng < pos.length - 1 ? pos[sblng + 1] : s_end;

		dl1.v0 = _f58;
		dl1.v2 = sblP;
		dl1.v3 = sblPp1;
		dl1.v4 = prntP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl1.v0 = _f59;
		dl1.v2 = sblP;
		dl1.v3 = sblPm1;
		dl1.v4 = prntP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));
		dl1.v0 = _f60;
		dl1.v2 = sblP;
		dl1.v3 = prntP;
		dl1.v4 = prntPp1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f61;
		dl1.v2 = sblP;
		dl1.v3 = prntP;
		dl1.v4 = prntPm1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f62;
		dl1.v2 = sblP;
		dl1.v3 = sblPp1;
		dl1.v4 = prntPm1;
		dl1.v5 = prntP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f63;
		dl1.v2 = sblPm1;
		dl1.v3 = sblP;
		dl1.v4 = prntPm1;
		dl1.v5 = prntP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f64;
		dl1.v2 = sblP;
		dl1.v3 = sblPp1;
		dl1.v4 = prntP;
		dl1.v5 = prntPp1;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f65;
		dl1.v2 = sblPm1;
		dl1.v3 = sblP;
		dl1.v4 = prntP;
		dl1.v5 = prntPp1;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f66;
		dl1.v2 = sblP;
		dl1.v3 = sblPp1;
		dl1.v4 = chldP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f67;
		dl1.v2 = sblP;
		dl1.v3 = sblPm1;
		dl1.v4 = chldP;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f68;
		dl1.v2 = sblP;
		dl1.v3 = chldP;
		dl1.v4 = chldPp1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f69;
		dl1.v2 = sblP;
		dl1.v3 = chldP;
		dl1.v4 = chldPm1;
		l = mf.calc5(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f70;
		dl1.v2 = sblP;
		dl1.v3 = sblPp1;
		dl1.v4 = chldPm1;
		dl1.v5 = chldP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f71;
		dl1.v2 = sblPm1;
		dl1.v3 = sblP;
		dl1.v4 = chldPm1;
		dl1.v5 = chldP;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f72;
		dl1.v2 = sblP;
		dl1.v3 = sblPp1;
		dl1.v4 = chldP;
		dl1.v5 = chldPp1;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		dl1.v0 = _f73;
		dl1.v2 = sblPm1;
		dl1.v3 = sblP;
		dl1.v4 = chldP;
		dl1.v5 = chldPp1;
		l = mf.calc6(dl1);
		l = dl1.calcs(s_dir, dir, l);
		f.add(li.l2i(l));

		

		if (_options.usedep) {
			int itype = 0;
			if (sblng == -1) {
				if (prnt == phead[chld]) {
					itype = 1;
				} else if (chld == phead[prnt]) {
					itype = 2;
				} else if (phead[prnt] == phead[chld]) {
					itype = 3;
				} else if ((phead[prnt] >= 0) && (phead[prnt] < phead.length)
						&& (phead[phead[prnt]] == chld)) {
					itype = 4;
				} else if ((phead[chld] >= 0) && (phead[chld] < phead.length)
						&& (phead[phead[chld]] == prnt)) {
					itype = 5;
				} else {
					itype = 6;
				}
			} else {
				if (prnt == phead[chld] && chld == phead[sblng]) {
					itype = 7;
				} else if (prnt == phead[chld] && prnt == phead[sblng]) {
					itype = 8;
				} else if (prnt == phead[sblng] && sblng == phead[chld]) {
					itype = 9;
				} else if (phead[sblng] == phead[chld]
						&& phead[sblng] == phead[prnt]) {
					itype = 10;
				} else if (phead[sblng] == prnt && phead[prnt] == phead[chld]) {
					itype = 11;
				} else if (phead[chld] == prnt && phead[prnt] == phead[sblng]) {
					itype = 12;
				} else if (phead[sblng] == prnt && phead[chld] > 0
						&& phead[chld] < phead.length
						&& phead[phead[chld]] == prnt) {
					itype = 13;
				} else if (phead[chld] == prnt && phead[sblng] > 0
						&& phead[sblng] < phead.length
						&& phead[phead[sblng]] == prnt) {
					itype = 14;
				} else {
					itype = 15;
				}

			}

			dlstlabel.v1 = label;
			dlstlabel.v0 = _f461;
			dlstlabel.v2 = prel[chld];
			dlstlabel.v3 = chldP;
			dlstlabel.v4 = sblP;
			dlstlabel.v5 = itype;
			l = mf.calc6(dlstlabel);
			l = dlstlabel.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
						
			dlstlabel.v1 = label;
			dlstlabel.v0 = _f462;
			dlstlabel.v2 = prel[chld];
			dlstlabel.v3 = prntP;
			dlstlabel.v4 = chldP;
			dlstlabel.v5 = sblP;
			dlstlabel.v6 = itype;
			l = mf.calc7(dlstlabel);
			l = dlstlabel.calcs(s_dir, dir, l);
			f.add(li.l2i(l));	
			
			dlstlabelw1.v1 = label;
			dlstlabelw1.v0 = _f463;
			dlstlabelw1.v2 = prel[chld];
			dlstlabelw1.v3 = prntW;
			dlstlabelw1.v4 = chldP;
			dlstlabelw1.v5 = sblP;
			dlstlabelw1.v6 = itype;
			l = mf.calc7(dlstlabelw1);
			l = dlstlabelw1.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dlstlabelw1.v1 = label;
			dlstlabelw1.v0 = _f464;
			dlstlabelw1.v2 = prel[chld];
			dlstlabelw1.v3 = chldW;
			dlstlabelw1.v4 = prntP;
			dlstlabelw1.v5 = sblP;
			dlstlabelw1.v6 = itype;
			l = mf.calc7(dlstlabelw1);
			l = dlstlabelw1.calcs(s_dir, dir, l);
			f.add(li.l2i(l));
			
			dlstlabelw1.v1 = label;
			dlstlabelw1.v0 = _f465;
			dlstlabelw1.v2 = prel[chld];
			dlstlabelw1.v3 = sblW;
			dlstlabelw1.v4 = prntP;
			dlstlabelw1.v5 = chldP;
			dlstlabelw1.v6 = itype;
			l = mf.calc7(dlstlabelw1);
			l = dlstlabelw1.calcs(s_dir, dir, l);
			f.add(li.l2i(l));

		}

		if (feats == null)
			return;
		/*
		 * short[] featsP =feats[prnt], featsD =feats[chld], featsSbl
		 * =sblng!=-1?feats[sblng]:null;
		 * 
		 * if(featsD != null && featsSbl != null && featsD.length == 2 &&
		 * featsSbl.length == 2 && featsD[0] == prnt && featsSbl[0] == prnt) {
		 * dlf.v0= _f4l4; dlf.v1=label; //dlf.v2=nofeat; dlf.v3=nofeat;
		 * dlf.v2=1; dlf.v3=featsD[1]; l= mf.calc4(dlf);
		 * l=dlf.calcs(s_dir,dir,l); f.add(li.l2i(l));
		 * 
		 * dlf.v0= _f4l5; dlf.v1=label; dlf.v2=prntP; dlf.v3=chldP;
		 * dlf.v4=sblP;dlf.v5=1; dlf.v6=featsD[1]; l= mf.calc7(dlf);
		 * l=dlf.calcs(s_dir,dir,l); f.add(li.l2i(l)); }
		 */

		if (_options.usecfg) {
			if (sblng == -1)
				return;
			int smaller = prnt < chld ? prnt : chld;
			int bigger = prnt < chld ? chld : prnt;
			int midder = -1;

			if (sblng < smaller) {
				midder = smaller;
				smaller = sblng;
			} else if (sblng > smaller && sblng < bigger) {
				midder = sblng;
			} else {
				midder = bigger;
				bigger = sblng;
			}

			int[] featsSmall = feats[smaller];
			int sentenceSize = forms.length;
			int featSize = (sentenceSize - smaller)
					* (sentenceSize - smaller - 1);
			if (featSize > 0 && featsSmall != null
					&& featsSmall.length == featSize) {
				int featSeq = (midder - smaller)
						* (2 * sentenceSize - midder - smaller - 1) / 2
						+ bigger - midder - 1;

				dlcf.v0 = _f4l8;
				dlcf.v1 = label; // dlcf.v2=nofeat; dlcf.v3=nofeat;
				dlcf.v2 = featsSmall[2 * featSeq];
				l = mf.calc3(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlcf.v0 = _f4l9;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq];
				dlcf.v3 = prntP;
				dlcf.v4 = chldP;
				dlcf.v5 = sblP;
				l = mf.calc6(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlcf.v0 = _f430;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq + 1];
				l = mf.calc3(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlcf.v0 = _f431;
				dlcf.v1 = label;
				dlcf.v2 = featsSmall[2 * featSeq + 1];
				dlcf.v3 = prntP;
				dlcf.v4 = chldP;
				dlcf.v5 = sblP;
				l = mf.calc6(dlcf);
				l = dlcf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlccf.v0 = _f432;
				dlccf.v1 = label;
				dlccf.v2 = featsSmall[2 * featSeq];
				dlccf.v3 = featsSmall[2 * featSeq + 1];
				l = mf.calc4(dlccf);
				l = dlccf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

				dlccf.v0 = _f433;
				dlccf.v1 = label;
				dlccf.v2 = featsSmall[2 * featSeq];
				dlccf.v3 = featsSmall[2 * featSeq + 1];
				dlccf.v4 = prntP;
				dlccf.v5 = chldP;
				dlccf.v6 = sblP;
				l = mf.calc7(dlccf);
				l = dlccf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

			}
		}

		/*
		 * if(_options.usedep) { int[] featsP =feats[prnt], featsD =feats[chld],
		 * featsSbl =sblng!=-1?feats[sblng]:null;
		 * 
		 * int sentenceSize = forms.length; int featsDSize = (sentenceSize-chld)
		 * * (sentenceSize-chld-1); int featsSBlSize = (sentenceSize-sblng) *
		 * (sentenceSize-sblng-1); //int featsDSize = (sentenceSize-chld-1);
		 * //int featsSBlSize = (sentenceSize-sblng-1);
		 * 
		 * if(featsD != null && featsSbl != null && featsD.length ==
		 * featsDSize+2 && featsSbl.length == featsSBlSize+2) { int s1 =
		 * (featsD[featsDSize] == prnt)? 1:0; int s2 = (featsSbl[featsSBlSize]
		 * == prnt)? 1:0; //int s1 = (featsD[featsDSize] == prnt)*2 +
		 * (featsSbl[featsSBlSize] == prnt); dlf2.v0= _f420; dlf2.v1=label;
		 * dlf2.v2=s1*2+s2; dlf2.v3=featsD[featsDSize+1];
		 * dlf2.v4=featsSbl[featsSBlSize+1]; l= mf.calc3(dlf2);
		 * l=dlf2.calcs(s_dir,dir,l); //l=dl1.calcs(s_dir,dir_gra,l);
		 * f.add(li.l2i(l));
		 * 
		 * dlf2.v0= _f421; dlf2.v1=label; dlf2.v2=s1*2+s2;
		 * dlf2.v3=featsD[featsDSize+1]; dlf2.v4=featsSbl[featsSBlSize+1];
		 * dlf2.v5=prntP; dlf2.v6=chldP;dlf2.v7=sblP; l= mf.calc8(dlf2);
		 * l=dlf2.calcs(s_dir,dir,l); //l=dl1.calcs(s_dir,dir_gra,l);
		 * f.add(li.l2i(l)); } }
		 */
		/*
		 * short[] featsP =feats[chld]; short[] featsSbl
		 * =sblng!=-1?feats[sblng]:null;
		 * 
		 * dlf.v0= _f75; dlf.v1=label; dlf.v2=sblP; dlf.v3=chldP; extractFeat(f,
		 * dir, featsP, featsSbl);
		 * 
		 * featsP =feats[prnt]; featsSbl =sblng!=-1?feats[sblng]:null;
		 * 
		 * dlf.v0= _f76; dlf.v1=label; dlf.v2=prntP; dlf.v3=sblP; if
		 * (featsP!=null && featsSbl!=null) { for(short
		 * i1=0;i1<featsP.length;i1++) { for(short i2=0;i2<featsSbl.length;i2++)
		 * { dlf.v4=featsP[i1]; dlf.v5=featsSbl[i2]; l= mf.calc6(dlf);
		 * l=dlf.calcs(s_dir,prnt<sblng?1:2,l); f.add(li.l2i(l)); } } } else if
		 * (featsP==null && featsSbl!=null) {
		 * 
		 * for(short i2=0;i2<featsSbl.length;i2++) { dlf.v4=nofeat;
		 * dlf.v5=featsSbl[i2]; l= mf.calc6(dlf); l=dlf.calcs(s_dir,dir,l);
		 * f.add(li.l2i(l)); }
		 * 
		 * } else if (featsP!=null && featsSbl==null) {
		 * 
		 * for(short i1=0;i1<featsP.length;i1++) { dlf.v4=featsP[i1];
		 * dlf.v5=nofeat; l= mf.calc6(dlf); l=dlf.calcs(s_dir,dir,l);
		 * f.add(li.l2i(l)); } }
		 */
	}

	private void extractFeat(IFV f, int dir, short[] featsP, short[] featsD) {
		long l;
		if (featsP != null && featsD != null) {
			for (short i1 = 0; i1 < featsP.length; i1++) {
				for (short i2 = 0; i2 < featsD.length; i2++) {
					dlf.v4 = featsP[i1];
					dlf.v5 = featsD[i2];
					l = mf.calc6(dlf);
					l = dlf.calcs(s_dir, dir, l);
					f.add(li.l2i(l));
				}
			}
		} else if (featsP == null && featsD != null) {

			for (short i2 = 0; i2 < featsD.length; i2++) {
				dlf.v4 = nofeat;
				dlf.v5 = featsD[i2];
				l = mf.calc6(dlf);
				l = dlf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

			}
		} else if (featsP != null && featsD == null) {

			for (short i1 = 0; i1 < featsP.length; i1++) {
				dlf.v4 = featsP[i1];
				dlf.v5 = nofeat;
				l = mf.calc6(dlf);
				l = dlf.calcs(s_dir, dir, l);
				f.add(li.l2i(l));

			}
		}
	}

	public FV encodeCat(short pposs[], int forms[], int[] lemmas,
			short[] heads, short[] types, int feats[][], short[] phead,
			short[] prel, FV f) {

		for (int i = 1; i < heads.length; i++) {

			basic(pposs, heads[i], i, f);
			first(pposs, forms, lemmas, feats, phead, prel, heads[i], i,
					types[i], f);

			int ch, cmi, cmo;
			if (heads[i] < i) {
				ch = rightmostRight(heads, heads[i], i);
				cmi = leftmostLeft(heads, i, heads[i]);
				cmo = rightmostRight(heads, i, heads.length);

			} else {
				ch = leftmostLeft(heads, heads[i], i);
				cmi = rightmostRight(heads, i, heads[i]);
				cmo = leftmostLeft(heads, i, 0);
			}
			sibling(pposs, forms, lemmas, feats, phead, prel, heads[i], i, ch,
					types[i], f);
			grandchildren(pposs, forms, lemmas, feats, phead, prel, heads[i],
					i, cmi, types[i], f);
			grandchildren(pposs, forms, lemmas, feats, phead, prel, heads[i],
					i, cmo, types[i], f);
		}

		return f;
	}

	public static float encode3(short[] pos, short heads[], short[] types,
			DataF d2) {

		float v = 0F;
		for (int i = 1; i < heads.length; i++) {

			int dir = (heads[i] < i) ? 0 : 1;

			v += d2.pl[heads[i]][i];
			v += d2.lab[heads[i]][i][types[i]][dir];

			boolean left = i < heads[i];
			short[] labels = Edges.get(pos[heads[i]], pos[i], left);
			int lid = -1;
			for (int k = 0; k < labels.length; k++)
				if (types[i] == labels[k]) {
					lid = k;
					break;
				}

			int ch, cmi, cmo;
			if (heads[i] < i) {
				ch = rightmostRight(heads, heads[i], i);
				cmi = leftmostLeft(heads, i, heads[i]);
				cmo = rightmostRight(heads, i, heads.length);

				if (ch == -1)
					ch = heads[i];
				if (cmi == -1)
					cmi = heads[i];
				if (cmo == -1)
					cmo = heads[i];

			} else {
				ch = leftmostLeft(heads, heads[i], i);
				cmi = rightmostRight(heads, i, heads[i]);
				cmo = leftmostLeft(heads, i, 0);

				if (ch == -1)
					ch = i;
				if (cmi == -1)
					cmi = i;
				if (cmo == -1)
					cmo = i;
			}
			v += d2.sib[heads[i]][i][ch][dir][lid];
			v += d2.gra[heads[i]][i][cmi][dir][lid];
			v += d2.gra[heads[i]][i][cmo][dir][lid];
		}
		return v;
	}

	private static int rightmostRight(short[] heads, int head, int max) {
		int rightmost = -1;
		for (int i = head + 1; i < max; i++)
			if (heads[i] == head)
				rightmost = i;

		return rightmost;
	}

	private static int leftmostLeft(short[] heads, int head, int min) {
		int leftmost = -1;
		for (int i = head - 1; i > min; i--)
			if (heads[i] == head)
				leftmost = i;
		return leftmost;
	}

	public static final String REL = "REL";
	private static final String END = "END";
	private static final String STR = "STR";
	private static final String LA = "LA";
	private static final String RA = "RA";

	private static int ra;
	private static int la;
	private static int s_str;
	private static int s_end;
	private static int s_stwrd;
	private static int s_relend;

	protected static final String TYPE = "TYPE";
	private static final String CHAR = "C";
	private static final String DIR = "D";
	public static final String POS = "POS";
	protected static final String DIST = "DIST";
	private static final String MID = "MID";

	private static final String _0 = "0";
	private static final String _4 = "4";
	private static final String _3 = "3";
	private static final String _2 = "2";
	private static final String _1 = "1";
	private static final String _5 = "5";
	private static final String _10 = "10";

	private static int di0, d4, d3, d2, d1, d5, d10;

	private static final String WORD = "WORD";

	private static final String STWRD = "STWRD";
	private static final String STPOS = "STPOS";

	private static final String _F1 = "F1", _F2 = "F2", _F3 = "F3", _F4 = "F4",
			_F5 = "F5", _F6 = "F6", _F7 = "F7", _F8 = "F8", _F9 = "F9",
			_F10 = "F10";
	private static final String _F11 = "F11", _F12 = "F12", _F13 = "F13",
			_F14 = "F14", _F15 = "F15", _F16 = "F16", _F17 = "F17",
			_F18 = "F18", _F19 = "F19", _F20 = "F20";
	private static final String _F21 = "F21", _F22 = "F22", _F23 = "F23",
			_F24 = "F24", _F25 = "F25", _F26 = "F26", _F27 = "F27",
			_F28 = "F28", _F29 = "F29", _F30 = "F30";
	private static final String _F31 = "F31", _F32 = "F32", _F33 = "F33",
			_F34 = "F34", _F35 = "F35", _F36 = "F36", _F37 = "F37",
			_F38 = "F38", _F39 = "F39", _F40 = "F40";
	private static final String _F41 = "F41", _F42 = "F42", _F43 = "F43",
			_F44 = "F44", _F45 = "F45", _F46 = "F46", _F47 = "F47",
			_F48 = "F48", _F49 = "F49", _F50 = "F50";
	private static final String _F51 = "F51", _F52 = "F52", _F53 = "F53",
			_F54 = "F54", _F55 = "F55", _F56 = "F56", _F57 = "F57",
			_F58 = "F58", _F59 = "F59", _F60 = "F60";
	private static final String _F61 = "F61", _F62 = "F62", _F63 = "F63",
			_F64 = "F64", _F65 = "F65", _F66 = "F66", _F67 = "F67",
			_F68 = "F68", _F69 = "F69", _F70 = "F70";
	private static final String _F71 = "F71", _F72 = "F72", _F73 = "F73",
			_F74 = "F74", _F75 = "F75", _F76 = "F76", _F77 = "F77",
			_F78 = "F78", _F79 = "F79";

	private static final String _F1l = "F1l", _F2l = "F2l", _F3l = "F3l",
			_F4l = "F4l", _F5l = "F5l", _F6l = "F6l", _F7l = "F7l",
			_F8l = "F8l", _F9l = "F9l", _F10l = "F10l";
	private static final String _F11l = "F11l", _F12l = "F12l", _F13l = "F13l",
			_F24l = "F24l", _F25l = "F25l", _F26l = "F26l", _F27l = "F27l",
			_F28l = "F28l", _F29l = "F29l", _F33l = "F33l", _F34l = "F34l",
			_F35l = "F35l", _F36l = "F36l", _F37l = "F37l", _F38l = "F38l";

	private static final String _F410 = "F410", _F411 = "F411", _F412 = "F412",
			_F413 = "F413", _F414 = "F414", _F415 = "F415", _F416 = "F416",
			_F417 = "F417", _F418 = "F418", _F419 = "F419";
	private static final String _F420 = "F420", _F421 = "F421", _F422 = "F422",
			_F423 = "F423", _F424 = "F424", _F425 = "F425", _F426 = "F426",
			_F427 = "F427", _F428 = "F428", _F429 = "F429";
	private static final String _F430 = "F430", _F431 = "F431", _F432 = "F432",
			_F433 = "F433", _F434 = "F434", _F435 = "F435", _F436 = "F436",
			_F437 = "F437", _F438 = "F438", _F439 = "F439";
	private static final String _F440 = "F440", _F441 = "F441", _F442 = "F445",
			_F443 = "F446", _F444 = "F444", _F445 = "F445", _F446 = "F446",
			_F447 = "F447", _F448 = "F448", _F449 = "F449";
	private static final String _F450 = "F450", _F451 = "F451", _F452 = "F452",
			_F453 = "F453", _F454 = "F454", _F455 = "F455", _F456 = "F456",
			_F457 = "F457", _F458 = "F458", _F459 = "F459";
	private static final String _F460 = "F460", _F461 = "F461", _F462 = "F462",
			_F463 = "F463", _F464 = "F464", _F465 = "F465", _F466 = "F466",
			_F467 = "F467", _F468 = "F468", _F469 = "F469";
	private static final String _F470 = "F470", _F471 = "F471", _F472 = "F472",
			_F473 = "F473", _F474 = "F474", _F475 = "F475", _F476 = "F476",
			_F477 = "F477", _F478 = "F478", _F479 = "F479";

	private static int _f1, _f2, _f3, _f4, _f5, _f6, _f7, _f8, _f9, _f10, _f11,
			_f12, _f13, _f14, _f15, _f16, _f17, _f18, _f19, _f20;
	private static int _f21, _f22, _f23, _f24, _f25, _f26, _f27, _f28, _f29,
			_f30, _f31, _f32, _f33, _f34, _f35, _f36, _f37, _f38, _f39, _f40,
			_f41;
	private static int _f42, _f43, _f44, _f45, _f46, _f47, _f48, _f49, _f50,
			_f51, _f52, _f53, _f54, _f55, _f56, _f57, _f58, _f59, _f60;
	private static int _f61, _f62, _f63, _f64, _f65, _f66, _f67, _f68, _f69,
			_f70, _f71, _f72, _f73, _f74, _f75, _f76, _f77, _f78, _f79, _f80;
	private static int nofeat;

	private static int _f1l, _f2l, _f3l, _f4l, _f5l, _f6l, _f7l, _f8l, _f9l,
			_f10l, _f11l, _f12l, _f13l, _f24l, _f25l, _f26l, _f27l, _f28l,
			_f29l, _f33l, _f34l, _f35l, _f36l, _f37l, _f38l;

	private static int _f4l0, _f4l1, _f4l2, _f4l3, _f4l4, _f4l5, _f4l6, _f4l7,
			_f4l8, _f4l9;
	private static int _f420, _f421, _f422, _f423, _f424, _f425, _f426, _f427,
			_f428, _f429;
	private static int _f430, _f431, _f432, _f433, _f434, _f435, _f436, _f437,
			_f438, _f439;
	private static int _f440, _f441, _f442, _f443, _f444, _f445, _f446, _f447,
			_f448, _f449;
	private static int _f450, _f451, _f452, _f453, _f454, _f455, _f456, _f457,
			_f458, _f459;
	private static int _f460, _f461, _f462, _f463, _f464, _f465, _f466, _f467,
			_f468, _f469;
	private static int _f470, _f471, _f472, _f473, _f474, _f475, _f476, _f477,
			_f478, _f479;
	private static OptionsSuper _options;

	/**
	 * Initialize the features.
	 * 
	 * @param maxFeatures
	 */
	static public void initFeatures(OptionsSuper options) {
		_options = options;
		MFO mf = new MFO();
		mf.register(POS, MID);
		s_str = mf.register(POS, STR);
		s_end = mf.register(POS, END);

		mf.register(TYPE, POS);

		s_stwrd = mf.register(WORD, STWRD);
		mf.register(POS, STPOS);

		la = mf.register(DIR, LA);
		ra = mf.register(DIR, RA);

		mf.register(TYPE, CHAR);

		mf.register(TYPE, Pipe.FEAT);
		nofeat = mf.register(Pipe.FEAT, "NOFEAT");

		s_relend = mf.register(REL, END);

		_f1 = mf.register(TYPE, _F1);
		_f2 = mf.register(TYPE, _F2);
		_f3 = mf.register(TYPE, _F3);
		_f4 = mf.register(TYPE, _F4);
		_f5 = mf.register(TYPE, _F5);
		_f6 = mf.register(TYPE, _F6);
		_f7 = mf.register(TYPE, _F7);
		_f8 = mf.register(TYPE, _F8);
		_f9 = mf.register(TYPE, _F9);
		_f10 = mf.register(TYPE, _F10);
		_f11 = mf.register(TYPE, _F11);
		_f12 = mf.register(TYPE, _F12);
		_f13 = mf.register(TYPE, _F13);
		_f14 = mf.register(TYPE, _F14);
		_f15 = mf.register(TYPE, _F15);
		_f16 = mf.register(TYPE, _F16);
		_f17 = mf.register(TYPE, _F17);
		_f18 = mf.register(TYPE, _F18);
		_f19 = mf.register(TYPE, _F19);
		_f20 = mf.register(TYPE, _F20);
		_f21 = mf.register(TYPE, _F21);
		_f22 = mf.register(TYPE, _F22);
		_f23 = mf.register(TYPE, _F23);
		_f24 = mf.register(TYPE, _F24);
		_f25 = mf.register(TYPE, _F25);
		_f26 = mf.register(TYPE, _F26);
		_f27 = mf.register(TYPE, _F27);
		_f28 = mf.register(TYPE, _F28);
		_f29 = mf.register(TYPE, _F29);
		_f30 = mf.register(TYPE, _F30);
		_f31 = mf.register(TYPE, _F31);
		_f32 = mf.register(TYPE, _F32);
		_f33 = mf.register(TYPE, _F33);
		_f34 = mf.register(TYPE, _F34);
		_f35 = mf.register(TYPE, _F35);
		_f36 = mf.register(TYPE, _F36);
		_f37 = mf.register(TYPE, _F37);
		_f38 = mf.register(TYPE, _F38);
		_f39 = mf.register(TYPE, _F39);
		_f40 = mf.register(TYPE, _F40);
		_f41 = mf.register(TYPE, _F41);

		_f42 = mf.register(TYPE, _F42);
		_f43 = mf.register(TYPE, _F43);
		_f44 = mf.register(TYPE, _F44);
		_f45 = mf.register(TYPE, _F45);
		_f46 = mf.register(TYPE, _F46);
		_f47 = mf.register(TYPE, _F47);
		_f48 = mf.register(TYPE, _F48);
		_f49 = mf.register(TYPE, _F49);

		_f50 = mf.register(TYPE, _F50);
		_f51 = mf.register(TYPE, _F51);
		_f52 = mf.register(TYPE, _F52);
		_f53 = mf.register(TYPE, _F53);
		_f54 = mf.register(TYPE, _F54);
		_f55 = mf.register(TYPE, _F55);
		_f56 = mf.register(TYPE, _F56);
		_f57 = mf.register(TYPE, _F57);
		_f58 = mf.register(TYPE, _F58);
		_f59 = mf.register(TYPE, _F59);

		_f60 = mf.register(TYPE, _F60);
		_f61 = mf.register(TYPE, _F61);
		_f62 = mf.register(TYPE, _F62);
		_f63 = mf.register(TYPE, _F63);
		_f64 = mf.register(TYPE, _F64);
		_f65 = mf.register(TYPE, _F65);
		_f66 = mf.register(TYPE, _F66);
		_f67 = mf.register(TYPE, _F67);
		_f68 = mf.register(TYPE, _F68);
		_f69 = mf.register(TYPE, _F69);

		_f70 = mf.register(TYPE, _F70);
		_f71 = mf.register(TYPE, _F71);
		_f72 = mf.register(TYPE, _F72);
		_f73 = mf.register(TYPE, _F73);
		_f74 = mf.register(TYPE, _F74);
		_f75 = mf.register(TYPE, _F75);
		_f76 = mf.register(TYPE, _F76);
		_f77 = mf.register(TYPE, _F77);
		_f78 = mf.register(TYPE, _F78);
		_f79 = mf.register(TYPE, _F79);

		_f1l = mf.register(TYPE, _F1l);
		_f2l = mf.register(TYPE, _F2l);
		_f3l = mf.register(TYPE, _F3l);
		_f4l = mf.register(TYPE, _F4l);
		_f5l = mf.register(TYPE, _F5l);
		_f6l = mf.register(TYPE, _F6l);
		_f7l = mf.register(TYPE, _F7l);
		_f8l = mf.register(TYPE, _F8l);
		_f9l = mf.register(TYPE, _F9l);
		_f10l = mf.register(TYPE, _F10l);
		_f11l = mf.register(TYPE, _F11l);
		_f12l = mf.register(TYPE, _F12l);
		_f13l = mf.register(TYPE, _F13l);

		_f24l = mf.register(TYPE, _F24l);
		_f25l = mf.register(TYPE, _F25l);
		_f26l = mf.register(TYPE, _F26l);
		_f27l = mf.register(TYPE, _F27l);
		_f28l = mf.register(TYPE, _F28l);
		_f29l = mf.register(TYPE, _F29l);

		_f33l = mf.register(TYPE, _F33l);
		_f34l = mf.register(TYPE, _F34l);
		_f35l = mf.register(TYPE, _F35l);
		_f36l = mf.register(TYPE, _F36l);
		_f37l = mf.register(TYPE, _F37l);
		_f38l = mf.register(TYPE, _F38l);

		_f4l0 = mf.register(TYPE, _F410);
		_f4l1 = mf.register(TYPE, _F411);
		_f4l2 = mf.register(TYPE, _F412);
		_f4l3 = mf.register(TYPE, _F413);
		_f4l4 = mf.register(TYPE, _F414);
		_f4l5 = mf.register(TYPE, _F415);
		_f4l6 = mf.register(TYPE, _F416);
		_f4l7 = mf.register(TYPE, _F417);
		_f4l8 = mf.register(TYPE, _F418);
		_f4l9 = mf.register(TYPE, _F419);

		_f420 = mf.register(TYPE, _F420);
		_f421 = mf.register(TYPE, _F421);
		_f422 = mf.register(TYPE, _F422);
		_f423 = mf.register(TYPE, _F423);
		_f424 = mf.register(TYPE, _F424);
		_f425 = mf.register(TYPE, _F425);
		_f426 = mf.register(TYPE, _F426);
		_f427 = mf.register(TYPE, _F427);
		_f428 = mf.register(TYPE, _F428);
		_f429 = mf.register(TYPE, _F429);

		_f430 = mf.register(TYPE, _F430);
		_f431 = mf.register(TYPE, _F431);
		_f432 = mf.register(TYPE, _F432);
		_f433 = mf.register(TYPE, _F433);
		_f434 = mf.register(TYPE, _F434);
		_f435 = mf.register(TYPE, _F435);
		_f436 = mf.register(TYPE, _F436);
		_f437 = mf.register(TYPE, _F437);
		_f438 = mf.register(TYPE, _F438);
		_f439 = mf.register(TYPE, _F439);

		_f440 = mf.register(TYPE, _F440);
		_f441 = mf.register(TYPE, _F441);
		_f442 = mf.register(TYPE, _F442);
		_f443 = mf.register(TYPE, _F443);
		_f444 = mf.register(TYPE, _F444);
		_f445 = mf.register(TYPE, _F445);
		_f446 = mf.register(TYPE, _F446);
		_f447 = mf.register(TYPE, _F447);
		_f448 = mf.register(TYPE, _F448);
		_f449 = mf.register(TYPE, _F449);

		_f450 = mf.register(TYPE, _F450);
		_f451 = mf.register(TYPE, _F451);
		_f452 = mf.register(TYPE, _F452);
		_f453 = mf.register(TYPE, _F453);
		_f454 = mf.register(TYPE, _F454);
		_f455 = mf.register(TYPE, _F455);
		_f456 = mf.register(TYPE, _F456);
		_f457 = mf.register(TYPE, _F457);
		_f458 = mf.register(TYPE, _F458);
		_f459 = mf.register(TYPE, _F459);

		_f460 = mf.register(TYPE, _F460);
		_f461 = mf.register(TYPE, _F461);
		_f462 = mf.register(TYPE, _F462);
		_f463 = mf.register(TYPE, _F463);
		_f464 = mf.register(TYPE, _F464);
		_f465 = mf.register(TYPE, _F465);
		_f466 = mf.register(TYPE, _F466);
		_f467 = mf.register(TYPE, _F467);
		_f468 = mf.register(TYPE, _F468);
		_f469 = mf.register(TYPE, _F469);

		_f470 = mf.register(TYPE, _F470);
		_f471 = mf.register(TYPE, _F471);
		_f472 = mf.register(TYPE, _F472);
		_f473 = mf.register(TYPE, _F473);
		_f474 = mf.register(TYPE, _F474);
		_f475 = mf.register(TYPE, _F475);
		_f476 = mf.register(TYPE, _F476);
		_f477 = mf.register(TYPE, _F477);
		_f478 = mf.register(TYPE, _F478);
		_f479 = mf.register(TYPE, _F479);

		di0 = mf.register(DIST, _0);
		d1 = mf.register(DIST, _1);
		d2 = mf.register(DIST, _2);
		d3 = mf.register(DIST, _3);
		d4 = mf.register(DIST, _4);
		d5 = mf.register(DIST, _5);
		// d5l=mf.register(DIST, _5l);
		d10 = mf.register(DIST, _10);
	}

}
