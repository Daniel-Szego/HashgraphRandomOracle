
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

	// SHARED STATE
	//shared state is a map of identifier -> integers (random number) 
 	private Map<String, Integer> randoms = new HashMap<String, Integer>();
	
	/** names and addresses of all members */
	private AddressBook addressBook;

	/** @return all the state information received so far from the network */
	public synchronized Map<String, Integer> getState() {
		return randoms;
	}

	
	
	/** @return all the strings received so far from the network, concatenated into one */
	public synchronized String getReceived() {
		String result = "";
		for (Map.Entry<String, Integer> entry : randoms.entrySet())
		{
			String key = entry.getKey();
			Integer value = entry.getValue();
			result += key + " " + value.toString() + " ";
		}
		return result;
	}
	
	

	/** @return the same as getReceived, so it returns the entire shared state as a single string */
	public synchronized String toString() {
		String result = "";
		for (Map.Entry<String, Integer> entry : randoms.entrySet())
		{
			String key = entry.getKey();
			Integer value = entry.getValue();
			result += key + " " + value.toString() + " ";
		}
		return result;	
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
			List<String> stringArray = new ArrayList<String>();
			List<Integer> intArray = new ArrayList<Integer>();
			
			for (Map.Entry<String, Integer> entry : randoms.entrySet())
			{
				String key = entry.getKey();
				Integer value = entry.getValue();
				stringArray.add(key);
				intArray.add(value);
			}

			Utilities.writeStringArray(outStream, 
					stringArray.toArray(new String[0]));

			int[] intArray2 = intArray.stream().mapToInt(i->i).toArray();

			Utilities.writeIntArray(outStream,
					intArray2);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(FCDataInputStream inStream) {
		try {
			List<String> stringArray = new ArrayList<String>(
					Arrays.asList(Utilities.readStringArray(inStream)));
			
			int[] intArray = Utilities.readIntArray(inStream);
			
			if (stringArray.size() != intArray.length) {
				throw new IOException("Size mismatch");	
			}
			
			for (int i = 0; i < stringArray.size(); i ++) {
				String key = stringArray.get(i);
				randoms.put(key, new Integer(intArray[i]));				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(SwirldState old) {
		randoms = new HashMap<String, Integer>(((RandomOracleState)old).randoms);
		addressBook = ((RandomOracleState) old).addressBook.copy();
	}

	@Override
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timestamp, byte[] transaction, Address address) {
		
		try {
			String transactionString = new String(transaction, StandardCharsets.UTF_8);
			String name = transactionString.substring(0, transactionString.indexOf("-")-1 );
			String integerString = transactionString.substring(transactionString.indexOf("-") + 2, transactionString.length());		
			randoms.put(name, new Integer(integerString));
			
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