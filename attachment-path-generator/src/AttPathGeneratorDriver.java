public class AttPathGeneratorDriver {

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Expected 1 argument as the item uuid.");
		} else {
			System.out.println("Using the UUID of "+ args[0]);
			System.out.println("The attachment path is " + (args[0].hashCode() & 127) + "/"+ args[0]);
		}
	}

}
