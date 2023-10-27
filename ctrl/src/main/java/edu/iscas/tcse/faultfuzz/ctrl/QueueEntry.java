package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;

public class QueueEntry {
	public String fname; // file name for the queue entry
	
	public FaultSequence faultSeq;
	public List<IOPoint> ioSeq;


	// public int fuzzed_time; // count to retrieve it from the queue

	public QueueEntry seed;
	public List<QueueEntry> mutates;

	public List<QueueEntry> on_recovery_mutates;
	public boolean on_recovery = false;

	// public Set<Integer> unique_io_id;
	public Set<Integer> recovery_io_id;
	public Set<Integer> not_tested_fault_id;

	public List<QueueEntry> favored_mutates;
	public boolean favored; // gy for mutate favored /* Currently favored? */
	// public boolean fs_redundant; /* Marked as redundant in the fs? */

	public int candidate_io;
	public int max_match_fault;


	public int bitmap_size; /* Number of bits set in bitmap */
	public long exec_s; /* Execution time (seconds) */
	public int handicap; /* Number of queue cycles behind */

	public List<FaultPoint> faultPointsToMutate;

	/**
	 * not stable
	 */
	public File workload;
	

	public QueueEntry() {
		mutates = new ArrayList<QueueEntry>();
		on_recovery_mutates = new ArrayList<QueueEntry>();
		favored_mutates = new ArrayList<QueueEntry>();
		// unique_io_id = new HashSet<Integer>();
		recovery_io_id = new HashSet<Integer>();
		not_tested_fault_id = new HashSet<Integer>();
	}

	public QueueEntry(FaultSequence faultSeq, List<IOPoint> ioSeq, boolean favored, int bitmap_size, long exec_s,
			int handicap) {
		this();
		this.faultSeq = faultSeq;
		this.ioSeq = ioSeq;
		this.favored = favored;
		this.bitmap_size = bitmap_size;
		this.exec_s = exec_s;
		this.handicap = handicap;
	}


	public String toJSONString() {
		String result = "transform QueueEntry to JSONString fail";
		result = JSONObject.toJSONString(this);
		return result;
	}

	public void calibrate() {
		this.max_match_fault = 0;
		this.candidate_io = 0;
		if (this.faultSeq == null || this.faultSeq.isEmpty()) {
			this.faultSeq = new FaultSequence();
			this.faultSeq.seq = new ArrayList<FaultPoint>();
		} else {
			// fix faultSeq
			// TODO: current comparison approch could cause problems
			// fault node in fault sequence should match the real node in io sequence
			for (; (this.candidate_io < this.ioSeq.size()) && this.max_match_fault < this.faultSeq.seq.size();) {
				if (this.ioSeq.get(this.candidate_io).CALLSTACK.toString().equals(this.faultSeq.seq.get(this.max_match_fault).ioPt.CALLSTACK.toString())
						&& this.ioSeq.get(this.candidate_io).appearIdx == this.faultSeq.seq.get(this.max_match_fault).ioPt.appearIdx) {
					this.faultSeq.seq.get(this.max_match_fault).ioPt = this.ioSeq.get(this.candidate_io);
					this.faultSeq.seq.get(this.max_match_fault).tarNodeIp = this.faultSeq.seq.get(this.max_match_fault).actualNodeIp;
					this.faultSeq.seq.get(this.max_match_fault).actualNodeIp = null;
					/**
					 * Wenhan Feng
					 * We should add an "actualParams" field if we still want to use global appearId to determine whether a I/O point
					 * should be injected a fault
					 */
					this.faultSeq.seq.get(this.max_match_fault).params = this.faultSeq.seq.get(this.max_match_fault).params;
					this.max_match_fault++;
				}
				this.candidate_io++;
			}

			/*
			 * Wenhan Feng
			 * I think we should not make candidate_io to 0
			 * I comment it.
			 */
			// this.ioSeq.subList(this.candidate_io, this.ioSeq.size());
			// this.candidate_io = 0;
		}

		this.faultSeq.reset();
	}


	private double calculateExecTimeScore() {
		double bound = 3;
		int avgExecuteSecond = FuzzInfo.total_execs == 0 ? 0 : (int) (FuzzInfo.exec_us / FuzzInfo.total_execs);
		double ratio = this.exec_s == 0 ? bound : (double) avgExecuteSecond / (double) this.exec_s;
		double score = Math.min(ratio, bound);
		// Stat.log("execTimeScore: " + score);
		return score;
	}

	private double calculateCovScore() {
		double bound = 3;
		int avgBitmapSize = FuzzInfo.total_bitmap_entries == 0 ? 0 : (int) (FuzzInfo.total_bitmap_size / FuzzInfo.total_bitmap_entries);
		double ratio =  avgBitmapSize == 0 ? bound : (double) this.bitmap_size / (double) avgBitmapSize ;
		double score = Math.min(ratio, bound);
		// Stat.log("covScore: " + score);
		return score;
	}

	private double calculateWaitingRoundScore() {
		double bound = 0.1;
		double ratio = FuzzInfo.total_execs == 0 ? 0 : (double) this.handicap / (double) FuzzInfo.total_execs;
		double score = Math.max(ratio, bound);
		// Stat.log("waitingRoundScore: " + score);
		return score;
	}

	private double calculateFaultScore() {
		double bound = 0.1;
		int faultsBound = 6;
		double score = 0;
		int faults = this.faultSeq.seq.size();
		// Stat.log("faults: " + faults);
		if (faults <= faultsBound) {
			score = faults;
		} else {
			double ratio =  1d / (double) (faults - 5) ;
			double base = Math.max(ratio, bound);
			score = base;
		}
		// Stat.log("faultScore: " + score);
		return score;
	}

	public int getPerfScore() {
		int perf_score = 0;

		double execTimeScore = calculateExecTimeScore();
		double covScore = calculateCovScore();
		double waitingRoundScore = calculateWaitingRoundScore();
		double faultScore = calculateFaultScore();

		int execTimeRadio = 10;
		int covRadio = 10;
		int waitingRoundRadio = 10;
		int faultRadio = 10;

		perf_score = (int) (execTimeScore * execTimeRadio + covScore * covRadio + waitingRoundScore * waitingRoundRadio + faultScore * faultRadio);

		Stat.debug("perf_score: " + perf_score);

		if (perf_score < 1) {
			perf_score = 1;
		}

		Stat.debug("perf_score: " + perf_score);

		if (perf_score > FuzzConf.HAVOC_MAX_MULT * 100)
			perf_score = FuzzConf.HAVOC_MAX_MULT * 100;
		return perf_score;
	}
}
