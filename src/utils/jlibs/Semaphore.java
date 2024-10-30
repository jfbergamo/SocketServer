package utils.jlibs;

public class Semaphore {

	private int count;

	public Semaphore(int n) {
		count = n;
	}

	synchronized public void P() {
		count--;
		try {
			if (count < 0) {
				wait();
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	
	synchronized public void V() {
		count++;
		notify();
	}
}
