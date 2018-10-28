
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.swirlds.platform.Browser;
import com.swirlds.platform.Console;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldMain;
import com.swirlds.platform.SwirldState;

/**
 * This HelloSwirld creates a single transaction, consisting of the string "Hello Swirld", and then goes
 * into a busy loop (checking once a second) to see when the state gets the transaction. When it does, it
 * prints it, too.
 */
public class RandomOracleMain implements SwirldMain {
	/** the platform running this app */
	public Platform platform;
	/** ID number for this member */
	public long selfId;
	/** a console window for text output */
	public Console console;
	/** sleep this many milliseconds after each sync */
	public final int sleepPeriod = 100;
		
	/**
	 * This is just for debugging: it allows the app to run in Eclipse. If the config.txt exists and lists a
	 * particular SwirldMain class as the one to run, then it can run in Eclipse (with the green triangle
	 * icon).
	 * 
	 * @param args
	 *            these are not used
	 */
	public static void main(String[] args) {
		Browser.main(args);
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public void preEvent() {
	}

	@Override
	public void init(Platform platform, long id) {
		this.platform = platform;
		this.selfId = id;
		//this.console = platform.createConsole(true); // create the window, make it visible
		this.console = platform.createConsole(true); // create the window, make it visible

		String[] pars = platform.getParameters();
		
		platform.setAbout("Decentralized Oracle v. 0.1\n"); // set the browser's "about" box
		platform.setSleepAfterSync(sleepPeriod);

	}	

	protected void LogException(Exception e) {
		console.out.println(e.toString() + " " +  e.getMessage() + " ");
	}
	
	protected void LogMessage(String message) {
		console.out.println(message);
	}
	
	@Override
	public void run() {
		try {
			String myName = platform.getState().getAddressBookCopy()
					.getAddress(selfId).getSelfName();
	
			
			console.out.println("Decentralized Random Oracle v.0.1");	
			console.out.println("My name is " + myName);
			
			Random rand = new Random();
			int  n = rand.nextInt(50000) + 1;
	
			console.out.println("Choosen Random is " + n);
			String transactionString = myName + " - " + n;	
			byte[] transaction = transactionString.getBytes(StandardCharsets.UTF_8);
			
			platform.createTransaction(transaction);
			String lastReceived = "";
		
			while (true) {
				RandomOracleState state = (RandomOracleState) platform
						.getState();
				String received = state.getReceived();				
				
				if (!lastReceived.equals(received)) {
					lastReceived = received;
					console.out.println("Received: " + received); // print all received transactions
				}
				try {
					Thread.sleep(sleepPeriod);
				} catch (Exception e) {
					LogException(e);
				}
			}

		} catch(Exception e){
			LogException(e);
		}
	}

	@Override
	public SwirldState newState() {
		return new RandomOracleState();
	}
	

	
}