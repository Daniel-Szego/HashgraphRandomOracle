
/*
 * This file is public domain.
 *
 * SWIRLDS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF 
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SWIRLDS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.FCDataInputStream;
import com.swirlds.platform.FCDataOutputStream;
import com.swirlds.platform.FastCopyable;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.swirlds.platform.Utilities;

/**
 * This holds the current state of the swirld. For this simple "hello swirld" code, each transaction is just
 * a string, and the state is just a list of the strings in all the transactions handled so far, in the
 * order that they were handled.
 */
public class RandomOracleState implements SwirldState {
	/**
	 * The shared state is just a list of the strings in all transactions, listed in the order received
	 * here, which will eventually be the consensus order of the community.
	 */
	private List<String> strings = new ArrayList<String>();
	/** names and addresses of all members */
	private AddressBook addressBook;

	//service database 
 	private Map<String, String> services = new HashMap<String, String>();

	//balances 
 	private Map<String, Integer> balances = new HashMap<String, Integer>();

	/** @return all the strings received so far from the network */
	public synchronized List<String> getStrings() {
		return strings;
	}

	
	
	/** @return all the strings received so far from the network, concatenated into one */
	public synchronized String getReceived() {
		return strings.toString();
	}
	
	public synchronized String getReceivedBalances() {
		   String fullState = "";
		   
		   Iterator it = balances.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        String key = pair.getKey().toString();
		        String value = pair.getValue().toString();
		        fullState += key + " " + value + " ";
		    }
		    return fullState;	
	}

	public synchronized String getReceivedServiceDB() {
		   String fullState = "";
		
		   Iterator its = services.entrySet().iterator();
		    while (its.hasNext()) {
		        Map.Entry pairs = (Map.Entry)its.next();
		        String key = pairs.getKey().toString();
		        String value = pairs.getValue().toString();
		        fullState += key + " " + value + " ";
		    }
		return fullState;	
	}
	

	/** @return the same as getReceived, so it returns the entire shared state as a single string */
	public synchronized String toString() {
		return strings.toString();
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public synchronized AddressBook getAddressBookCopy() {
		return addressBook.copy();
	}

	@Override
	public synchronized FastCopyable copy() {
		RandomOracleState copy = new RandomOracleState();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public synchronized void copyTo(FCDataOutputStream outStream) {
		try {
			Utilities.writeStringArray(outStream,
					strings.toArray(new String[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(FCDataInputStream inStream) {
		try {
			strings = new ArrayList<String>(
					Arrays.asList(Utilities.readStringArray(inStream)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(SwirldState old) {
		strings = new ArrayList<String>(((RandomOracleState) old).strings);
		services = new HashMap<String, String>(((RandomOracleState) old).services);
		balances = new HashMap<String, Integer>(((RandomOracleState) old).balances);
		addressBook = ((RandomOracleState) old).addressBook.copy();
	}

	@Override
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timestamp, byte[] transaction, Address address) {
		
		try {
			String transactionString = new String(transaction, StandardCharsets.UTF_8);
			
			if (transactionString.startsWith("addservice")){
					String name = transactionString.substring(transactionString.indexOf("name:") + 5, transactionString.indexOf("service:") - 1);
					String serviceName = transactionString.substring(transactionString.indexOf("service:") + 9);
					services.put(name, serviceName);									
			}else if (transactionString.startsWith("useservice")) {
				String name = transactionString.substring(transactionString.indexOf("name:") + 5, transactionString.indexOf("service:") - 1);
				String serviceName = transactionString.substring(transactionString.indexOf("service:") + 9);
				balances.put(name, balances.get(name) - 1);		
			}else if (transactionString.startsWith("initbalance")) {
					String name = transactionString.substring(transactionString.indexOf("name:") + 5, transactionString.indexOf("balance:") - 1);
					Integer balance = Integer.parseInt(transactionString.substring(transactionString.indexOf("balance:") + 9));
					balances.put(name, balance);					
			}else{	
				strings.add(new String(transaction, StandardCharsets.UTF_8));	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void noMoreTransactions() {
	}

	@Override
	public synchronized void init(Platform platform, AddressBook addressBook) {
		this.addressBook = addressBook;
	}
}