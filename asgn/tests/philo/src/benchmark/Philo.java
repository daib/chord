package benchmark;

class Table {
    boolean forks[];
    int eatctr;

	Table() {
		forks = new boolean[Philo.NUM_PHIL];
		for (int i = 0; i < Philo.NUM_PHIL; ++i)
			forks[i] = true;
	}

	synchronized int getForks(int id) throws InterruptedException {
		int id1 = id;
		int id2 = (id + 1) % Philo.NUM_PHIL;
		while(!(forks[id1] && forks[id2])) {
			wait();
		}
		forks[id1] = forks[id2] = false;
		return eatctr++;
	}
  
	synchronized void putForks(int id) {
		forks[id] = forks[(id + 1) % Philo.NUM_PHIL] = true;
		notify();
	}
}

class Philo extends Thread {
    static final int NUM_PHIL = 5;
    static final int MAX_EAT = 12;
    int id;
    Table t;

    Philo(int id, Table t) {
		this.id = id;
		this.t = t;
    }
                
    public void run() {
		try {
			int max_eat = 0;
			while (max_eat <  MAX_EAT) {
				max_eat = t.getForks(id);
				long l = (int)(Math.random() * 500) + 20;
				sleep(l);
				t.putForks(id);
			}
		} catch(InterruptedException e) { }
	}

    public static void main(String args[]) throws Exception {
		Table tab = new Table();
		Philo[] p = new Philo[NUM_PHIL];
		for (int i = 0; i < NUM_PHIL; ++i) {
			p[i] = new Philo(i, tab);
			p[i].start();
		}
		for (int i = 0; i < NUM_PHIL; ++i) 
			p[i].join();
	}
}







