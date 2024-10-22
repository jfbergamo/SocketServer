package utils.jlibs;

public class MutexSemaphore {

	private int count;

	public MutexSemaphore() {
		count = 1;
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
