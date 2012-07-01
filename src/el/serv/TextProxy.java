package el.serv;

import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.StringTokenizer;

/**
 * Convert method calls on interface to text strings.
 * Only a few primitive types and strings excluding control chars and tilde supported.
 * Always use same charset on sending and receiving side (UTF-8).
 */
public class TextProxy implements InvocationHandler {
	
	public interface Unproxy {
		public void call(String line);
	}
	
	/**
	 * Create proxy for converting methods calls on interface into strings
	 */
	public static <T> T createProxy(Class<T> iface, PrintWriter pw) {
		Object proxy = Proxy.newProxyInstance(
				iface.getClassLoader(), 
				new Class<?>[] { iface }, 
				new TextProxy(pw));
		return (T) proxy;
	}
	
	/**
	 * Create unproxy for converting strings back into method call on real object
	 */
	public static <T> Unproxy createUnproxy(final Class<T> iface, final T impl) {
		return new Unproxy() {
			private final Method[] methods = iface.getMethods();
			@Override
			public void call(String line) {
				unproxy(methods, impl, line);
			}
		};
	}
	
	/**
	 * Convert string into method call on real implementation
	 */
	private static <T> void unproxy(Method[] methods, T impl, String line) {
		StringTokenizer tokens = new StringTokenizer(line);
		String methodName = tokens.nextToken();
		
		// get the method
		// should probably cache these
		//Method[] methods = iface.getMethods();
		Method method = null;
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				method = m;
				break;
			}
		}
		
		if (method == null) {
			throw new RuntimeException("could not find method " + methodName);
		}
		
		// get the params
		Class<?>[] paramTypes = method.getParameterTypes();
		Object[] params = new Object[paramTypes.length];
		for (int n = 0; n < paramTypes.length; n++) {
			Class<?> p = paramTypes[n];
			if (p == String.class) {
				params[n] = tokens.nextToken().replace("~", " ");
			} else if (p == long.class) {
				params[n] = Long.valueOf(tokens.nextToken());
			} else if (p == int.class) {
				params[n] = Integer.valueOf(tokens.nextToken());
			} else if (p == float.class) {
				params[n] = Float.valueOf(tokens.nextToken());
			} else if (p == boolean.class) {
				params[n] = Boolean.valueOf(tokens.nextToken());
			} else {
				throw new RuntimeException("unknown type " + p);
			}
		}
		
		if (tokens.hasMoreTokens()) {
			throw new RuntimeException("did not read all parameters from " + line);
		}
		
		try {
			method.invoke(impl, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private final PrintWriter pw;

	private TextProxy(PrintWriter w) {
		this.pw = w;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getName());
		if (args != null) {
			for (Object arg : args) {
				sb.append(" ");
				if (arg instanceof String) {
					// make it one token
					sb.append(((String)arg).replace(" ", "~"));
				} else {
					sb.append(arg);
				}
			}
		}
		System.out.println("write: " + sb);
		pw.println(sb);
		pw.flush();
		// FIXME exception here will prevent other clients getting called in loop
		// could catch all but need to indicate that removal is needed
		return null;
	}
	
}
