
public class TestFilePath {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String cur_path = "hdfs://C2hb-hdfs:9000/hbase/.tmp/data/default/FAVMyInfo/7bf21636b1cc168d7242371fb5e94660/.regioninfo";
		String error = "hdfs://C2hb-hdfs:9000/hbase/.tmp/data/default/FAVMyInfo/738d11ee18e177d8531b26343a707a7c/.regioninfo";
		System.out.println(likelySamePath(cur_path, error));
		System.out.println(cur_path);
		System.out.println(isHexNumberRex(cur_path));
		String num = "123412341234";
		System.out.println(isHexNumberRex(num));
		String cur_path2 = "hdfs://C2hb-hdfs:9000/hbase/.tmp/data/default/AnotherTable/7bf21636b1cc168d7242371fb5e94660/.regioninfo";
		System.out.println(likelySamePath(cur_path, cur_path2));
		
		String str3 = "hdfs://NN:9000/user/root/gyOutput/_temporary/1/_temporary/attempt_1626750179562_0001_r_000000_0/part-r-00000";
		String str4 = "hdfs://NN:9000/user/root/gyOutput/_temporary/1/_temporary/attempt_1626750171234_0001_r_000000_0/part-r-00000";
		
		String yarnpath = "/home/gaoyu/evaluation/hadoop-3.2.2/rmstore/FSRMStateRoot/RMAppRoot/application_1623899635558_0001/application_1623899635558_0001.tmp";
		
		String yarnpaht2 = "LVJNIDB:/home/gaoyu/evaluation/hadoop-3.2.2/nmstore/yarn-nm-state/NMTokens/appattempt_1623899635558_0001_000001";
		
		String yarnpaht3 = "LVJNIDB:/home/gaoyu/evaluation/hadoop-3.2.2/nmstore/yarn-nm-state/Localization/private/root/appcache/application_1623899635558_0001/started//home/gaoyu/evaluation/hadoop-3.2.2/nmdir/usercache/root/appcache/application_1623899635558_0001/filecache/12/job.split";
		
		String yarnpaht4 = "LVJNIDB:/home/gaoyu/evaluation/hadoop-3.2.2/nmstore/yarn-nm-state/Localization/private/root/appcache/application_1623899635558_0001/completed//home/gaoyu/evaluation/hadoop-3.2.2/nmdir/usercache/root/appcache/application_1623899635558_0001/filecache/10/job.splitmetainfo";
	
		String yarnpaht4_ = "LVJNIDB:/home/gaoyu/evaluation/hadoop-3.2.2/nmstore/yarn-nm-state/Localization/private/root/appcache/application_1623899631111_0001/completed//home/gaoyu/evaluation/hadoop-3.2.2/nmdir/usercache/root/appcache/application_1623899631111_0001/filecache/10/job.splitmetainfo";
		
	    System.out.println(likelySamePath(str3, str4));
	    System.out.println(likelySamePath(yarnpaht4, yarnpaht4_));
	}

	private static boolean isHexNumberRex(String str){
		String validate = "(?i)[0-9a-f]+";
		return str.matches(validate);
	}

	
	//-1: not similar
	//0: total same
	//1: similar path
	//for substring, tolerate at most 1 similar difference
	private static int similarStringWithSign(String str1, String str2, String sign){
		int diff = 0;
		if(!str1.contains(sign) || !str2.contains(sign)) {
			return -1;
		}
		String[] sec1 = str1.split(sign);
		String[] sec2 = str2.split(sign);
		
		if(sec1.length  != sec2.length) {
			return -1;
		}
		for(int i = 0; i< sec1.length; i++) {
			if(!sec1[i].equals(sec2[i])) {
				if(isHexNumberRex(sec1[i]) && isHexNumberRex(sec2[i])) {
					diff++;
					if(diff > 1) {
						return -1;
					} else {
						continue;
					}
				} else {
					return -1;
				}
			}
		}
		if(diff == 1) {
			return 1;
		} else {
			return 0;
		}
	}
	
	//can tolerate at most 2 similar differences
	private static boolean likelySamePath(String str1, String str2){
		String[] secs1 = str1.split("/");
		System.out.println(secs1.length+" "+secs1[0]);
		String[] secs2 = str2.split("/");
		System.out.println(secs2.length+" "+secs2[0]);
		int diff = 0;
		if(secs1.length != secs2.length) {
			return false;
		}
		for(int i = 0; i< secs1.length; i++) {
			if(!secs1[i].equals(secs2[i])) {
  				if(isHexNumberRex(secs1[i]) && isHexNumberRex(secs2[i])) {
					diff++;
					if(diff>2) {
						return false;
					}
				} else {
					int similar = similarStringWithSign(secs1[i], secs2[i], "-");
	  				if(similar == 1) {
	  					diff++;
	  					if(diff>2) {
	  						return false;
	  					}
	  				} else if (similar == -1) {
	  					similar = similarStringWithSign(secs1[i], secs2[i], "_");
	  					if(similar == 1) {
	  						diff++;
	  						if(diff>2) {
	  							return false;
	  						}
	  					} else if (similar == -1) {
	  						return false;
	  					}
	  				}
				}
  			}
		}
		return true;
	}
}
