package is2.parser;

import is2.data.DataF;

import java.util.ArrayList;

/**
 * @author Bernd Bohnet, 30.08.2009
 * 
 *         This class implements a parallel feature extractor.
 */
final public class ParallelDecoderNBest extends Thread {
	// some constants
	private static final float INIT_BEST = (-1.0F / 0.0F);
	private static final boolean[] DIR = { false, true };

	// the data space of the weights for a dependency tree
	final private DataF x;

	private short[] pos;

	private Open O[][][][][];
	private Closed C[][][][][];

	private int n;
	private int nbest;

	boolean done = false;
	public boolean waiting = false;

	/**
	 * Initialize the parallel decoder.
	 * 
	 * @param pos
	 *            part-of-speech
	 * @param d
	 *            data
	 * @param edges
	 *            part-of-speech edge mapping
	 * @param o
	 *            open spans
	 * @param c
	 *            closed spans
	 * @param n
	 *            number of words
	 */
	public ParallelDecoderNBest(short[] pos, DataF d, Open o[][][][][],
			Closed c[][][][][], int n, int nbest) {

		this.pos = pos;
		this.x = d;

		this.O = o;
		this.C = c;
		this.n = n;
		this.nbest = nbest;
	}

	private static class DSet {
		short w1, w2;
	}

	@Override
	public void run() {

		while (true) {

			DSet set = get();
			if (done && set == null)
				break;

			if (set == null) {
				if (!done)
					waiting = true;

				while (waiting)
					yield();
				continue;
			}

			short s = set.w1, t = set.w2;

			for (short dir = 1; dir >= 0; dir--) {

				short[] labs = (dir == 1) ? Edges.get(pos[s], pos[t], false)
						: Edges.get(pos[t], pos[s], true);

				O[s][t][dir] = new Open[labs.length][nbest];
				int labelNum = O[s][t][dir].length;
				for (int l = O[s][t][dir].length - 1; l >= 0; l--) {
					KBestListOpen kopens = new KBestListOpen(nbest);
					// for (int k = 0; k < nbest; k++) {

					for (int r = s; r < t; r++) {

						if (s == 0 && r != 0)
							continue;
						// double tRP = INIT_BEST;
						// Closed tL = null, tR = null;
						double tLPr = INIT_BEST, tRPr = INIT_BEST;
						Closed tLCld = null, tRCld = null;

						if (r == s && r == t - 1) {
							tLPr = dir == 1 ? x.sib[s][t][s][0][l]
									: x.gra[t][s][s][1][l];
							tRPr = dir == 1 ? x.gra[s][t][s][0][l]
									: x.sib[t][s][s][1][l];

							kopens.add(new Open(
									s,
									t,
									dir,
									labs[l],
									tLCld,
									tRCld,
									(float) (tLPr
											+ tRPr
											+ ((dir == 1) ? x.pl[s][t]
													: x.pl[t][s]) + ((dir == 1) ? x.lab[s][t][labs[l]][0]
											: x.lab[t][s][labs[l]][1]))));
						}

						if (r == s && r != t - 1) {
							tLPr = dir == 1 ? x.sib[s][t][s][0][l]
									: x.gra[t][s][s][1][l];

							for (int i = r + 1; i < t; i++) {
								for (int k = 0; k < nbest; k++) {
									// if (((dir == 1 ? x.gra[s][t][i][0][l]
									// : x.sib[t][s][i][1][l]) + C[r +
									// 1][t][0][i][k].p) > tRPr) {
									tRPr = ((dir == 1 ? x.gra[s][t][i][0][l]
											: x.sib[t][s][i][1][l]) + C[r + 1][t][0][i][k].p);
									tRCld = C[r + 1][t][0][i][k];
									if(tRCld.bfaked)break;
									boolean data_added  = kopens.add(new Open(
											s,
											t,
											dir,
											labs[l],
											tLCld,
											tRCld,
											(float) (tLPr
													+ tRPr
													+ ((dir == 1) ? x.pl[s][t]
															: x.pl[t][s]) + ((dir == 1) ? x.lab[s][t][labs[l]][0]
													: x.lab[t][s][labs[l]][1]))));
									if(!data_added)break;
									// }
								}

							}
						}

						if (r != s && r == t - 1) {
							tRPr = dir == 1 ? x.gra[s][t][s][0][l]
									: x.sib[t][s][s][1][l];

							for (int i = s + 1; i <= r; i++) {
								for (int k = 0; k < nbest; k++) {
									// if (((dir == 1 ? x.sib[s][t][i][0][l]
									// : x.gra[t][s][i][1][l]) +
									// C[s][r][1][i][k].p) > tLPr) {
									tLPr = ((dir == 1 ? x.sib[s][t][i][0][l]
											: x.gra[t][s][i][1][l]) + C[s][r][1][i][k].p);
									tLCld = C[s][r][1][i][k];
									if(tLCld.bfaked)break;
									// }
									boolean data_added  = kopens.add(new Open(
											s,
											t,
											dir,
											labs[l],
											tLCld,
											tRCld,
											(float) (tLPr
													+ tRPr
													+ ((dir == 1) ? x.pl[s][t]
															: x.pl[t][s]) + ((dir == 1) ? x.lab[s][t][labs[l]][0]
													: x.lab[t][s][labs[l]][1]))));
									if(!data_added)break;
								}

							}
						}

						if (r != s && r != t - 1) {
							for (int i1 = s + 1; i1 <= r; i1++) {
								for (int i2 = r + 1; i2 < t; i2++) {
									for (int k1 = 0; k1 < nbest; k1++) {
										boolean zero_data_added = false;
										for (int k2 = 0; k2 < nbest; k2++) {
											tLPr = ((dir == 1 ? x.sib[s][t][i1][0][l]
													: x.gra[t][s][i1][1][l]) + C[s][r][1][i1][k1].p);
											tLCld = C[s][r][1][i1][k1];
											tRPr = ((dir == 1 ? x.gra[s][t][i2][0][l]
													: x.sib[t][s][i2][1][l]) + C[r + 1][t][0][i2][k2].p);
											tRCld = C[r + 1][t][0][i2][k2];
											if(tLCld.bfaked || tRCld.bfaked)
											{
												if(k2 == 0)zero_data_added = true;
												break;
											}
											boolean data_added  = kopens.add(new Open(
													s,
													t,
													dir,
													labs[l],
													tLCld,
													tRCld,
													(float) (tLPr
															+ tRPr
															+ ((dir == 1) ? x.pl[s][t]
																	: x.pl[t][s]) + ((dir == 1) ? x.lab[s][t][labs[l]][0]
															: x.lab[t][s][labs[l]][1]))));
											if(!data_added)
											{
												if(k2 == 0)zero_data_added = true;
												break;
											}
										}
										if(zero_data_added)break;
									}
								}
							}
						}
					}
					for (int k = 0; k < nbest; k++) {
						O[s][t][dir][l][k] = kopens.get(k);
					}
				}
			}

			C[s][t][1] = new Closed[n][nbest];
			C[s][t][0] = new Closed[n][nbest];

			for (int m = s; m <= t; m++) {
				for (boolean d : DIR) {
					if ((d && m != s) || !d && (m != t && s != 0)) {

						KBestListClosed kcloseds = new KBestListClosed(nbest);
						// create closed structure
						// double top = INIT_BEST;

						int numLabels = O[(d ? s : m)][(d ? m : t)][d ? 1 : 0].length;

						// for (int l = numLabels-1; l >=0; l--) {
						for (int l = 0; l < numLabels; l++) {
							
							if ((m == (d ? t : s))) {							
								for (int k1 = 0; k1 < nbest; k1++) {
									boolean zero_data_added = false;
									double top = INIT_BEST;
									Open tU = null;
									Closed tL = null;
									Open hi = O[(d ? s : m)][(d ? m : t)][d ? 1 : 0][l][k1];
									if(hi.bfaked)break;
									
									top = (hi.p + x.gra[(d ? s : t)][m][d ? s
											: t][d ? 0 : 1][l]);
									tU = hi;
									tL = null;
									boolean outer_data_added  = kcloseds.add(new Closed(s, t, m, d ? 1 : 0,
											tU, tL, (float) top));
									if(!outer_data_added)break;
								}
							}
							else
							{
								for (int amb = m + (d ? 1 : -1); amb != (d ? t
										: s) + (d ? 1 : -1); amb += (d ? 1 : -1)) {								
									for (int k1 = 0; k1 < nbest; k1++) {
										boolean zero_data_added = false;
										double top = INIT_BEST;
										Open tU = null;
										Closed tL = null;
										Open hi = O[(d ? s : m)][(d ? m : t)][d ? 1 : 0][l][k1];
										if(hi.bfaked)break;
										for (int k2 = 0; k2 < nbest; k2++) {
											top = (hi.p
													+ C[d ? m : s][d ? t : m][d ? 1
															: 0][amb][k2].p + x.gra[d ? s
													: t][m][amb][(d ? 0 : 1)][l]);
											tU = hi;
											tL = C[d ? m : s][d ? t : m][d ? 1 : 0][amb][k2];
											if(tL.bfaked)
											{
												if(k2 == 0)zero_data_added = true;
												break;
											}	
											boolean data_added  = kcloseds.add(new Closed(s, t, m, d ? 1
													: 0, tU, tL, (float) top));
											
											if(!data_added)
											{
												if(k2 == 0)zero_data_added = true;
												break;
											}
										}								
										if(zero_data_added)
										{
											break;
										}
										
									}
								}
								
							}														
						}

						for (int k = 0; k < nbest; k++) {
							C[s][t][d ? 1 : 0][m][k] = kcloseds.get(k);
						}
						// C[s][t][d ? 1 : 0][m] = new Closed(s, t, m, d ? 1 :
						// 0,
						// tU, tL, (float) top);
					}
				}
			}
		}
	}

	public static ArrayList<DSet> sets = new ArrayList<DSet>();

	static synchronized private DSet get() {
		synchronized (sets) {
			if (sets.size() == 0)
				return null;
			return sets.remove(sets.size() - 1);
		}
	}

	public void add(short w1, short w2) {
		DSet ds = new DSet();
		ds.w1 = w1;
		ds.w2 = w2;
		sets.add(ds);
	}
}

// Max Heap
// We know that never more than K elements on Heap
class KBestListOpen {
	private int size;
	private Open[] theArray;

	public KBestListOpen(int def_cap) {
		size = def_cap;
		theArray = new Open[size];
		for (int i = 0; i < size; i++) {
			theArray[i] = new Open();
		}

	}

	public Open get(int index) {
		if (index >= 0 && index < size) {
			return theArray[index];
		} else {
			return null;
		}
	}

	public boolean add(Open e) {
		if (e.compareTo(theArray[size - 1]) < 0) {
			return false;
		}
		int insertp = -1;

		if (e.compareTo(theArray[0]) > 0) {
			insertp = 0;
		} else {
			int prev = 0, end = size - 1, mid = -1;
			while (prev < end - 1) {
				mid = (prev + end) / 2;
				if (e.compareTo(theArray[mid]) > 0) {
					end = mid;
				}
				if (e.compareTo(theArray[mid]) <= 0) {
					prev = mid;
				}
			}
			insertp = end;
		}

		for (int i = size - 1; i > insertp; i--) {
			theArray[i] = theArray[i - 1];
		}
		theArray[insertp] = e;
		
		return true;
	}
}

// Max Heap
// We know that never more than K elements on Heap
class KBestListClosed {
	private int size;
	private Closed[] theArray;

	public KBestListClosed(int def_cap) {
		size = def_cap;
		theArray = new Closed[size];
		for (int i = 0; i < size; i++) {
			theArray[i] = new Closed();
		}

	}

	public Closed get(int index) {
		if (index >= 0 && index < size) {
			return theArray[index];
		} else {
			return null;
		}
	}

	public boolean add(Closed e) {
		if (e.compareTo(theArray[size - 1]) < 0) {
			return false;
		}
		int insertp = -1;

		if (e.compareTo(theArray[0]) > 0) {
			insertp = 0;
		} else {
			int prev = 0, end = size - 1, mid = -1;
			while (prev < end - 1) {
				mid = (prev + end) / 2;
				if (e.compareTo(theArray[mid]) > 0) {
					end = mid;
				}
				if (e.compareTo(theArray[mid]) <= 0) {
					prev = mid;
				}
			}
			insertp = end;
		}

		for (int i = size - 1; i > insertp; i--) {
			theArray[i] = theArray[i - 1];
		}
		theArray[insertp] = e;
		return true;
	}
}
