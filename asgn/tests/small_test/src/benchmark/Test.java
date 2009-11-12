package benchmark;

public class Test {
	public static void main(String[] args) {
		String[] a = new String[] { "a1", "a2" };
		String[] b = new String[] { "b1", "b2" };
		MyList<String> l;
		l = new MyList<String>();
		for (int i = 0; i < a.length ;i++) {
			String v1 = a[i];
			l.append(v1);
		}
		l = new MyList<String>();
		for (int i = 0; i < b.length ;i++) {
			String v2 = b[i];
			l.append(v2);
		}
	}
}

class Link<T> {
    T data;
    Link<T> next;
}

class MyList<T> {
    Link<T> tail;
    void append(T c) {
        Link<T> k = new Link<T>();
        k.data = c;
        Link<T> t = this.tail;
        if (t != null)
			t.next = k;
        this.tail = k;
    }
}
