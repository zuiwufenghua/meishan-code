package ensemble;

import java.util.Date;

public class Now extends Date {
	private static final long serialVersionUID = 1L;

	public Now() {
		super(System.currentTimeMillis());
	}
}
