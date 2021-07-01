package Banker;

/**
 * The Bank
 */
public class BankImpl implements Bank {

    private int[] Available_Resources;
    private int[][] Maximum_Resources;
    private int[][] Allocated_Resources;
    private int N_Customers;
    private int M_Resources;
    private int[][] Needed_Resources;

    boolean safestate = false;

    public BankImpl(int[] Resources) {

 
        M_Resources = Resources.length;
        N_Customers = Customer.COUNT;

        Available_Resources = new int[M_Resources];
        System.arraycopy(Resources, 0, Available_Resources, 0, M_Resources);

        Maximum_Resources = new int[ Customer.COUNT][];
        Allocated_Resources = new int[ Customer.COUNT][];
        Needed_Resources = new int[ Customer.COUNT][];

    }

    @Override
    public void addCustomer(int threadNum, int[] maxDemand) {

        Maximum_Resources[threadNum] = new int[M_Resources];
        Allocated_Resources[threadNum] = new int[M_Resources];
        Needed_Resources[threadNum] = new int[M_Resources];

        System.arraycopy(maxDemand, 0, Maximum_Resources[threadNum], 0, maxDemand.length);
        System.arraycopy(maxDemand, 0, Needed_Resources[threadNum], 0, maxDemand.length);

    }

    @Override
    public void getState() {

        System.out.print("Available = \t[");
        for (int i = 0; i < M_Resources - 1; i++) {
            System.out.print(Available_Resources[i] + " ");
        }
        System.out.println(Available_Resources[M_Resources - 1] + "]");
        System.out.print("\nAllocation = \t");
        for (int i = 0; i < N_Customers; i++) {
            System.out.print("[");
            for (int j = 0; j < M_Resources - 1; j++) {
                System.out.print(Allocated_Resources[i][j] + " ");
            }
            System.out.print(Allocated_Resources[i][M_Resources - 1] + "]");
        }
        System.out.print("\nMax = \t\t");
        for (int i = 0; i < N_Customers; i++) {
            System.out.print("[");
            for (int j = 0; j < M_Resources - 1; j++) {
                System.out.print(Maximum_Resources[i][j] + " ");
            }
            System.out.print(Maximum_Resources[i][M_Resources - 1] + "]");
        }
        System.out.print("\nNeed = \t\t");
        for (int i = 0; i < N_Customers; i++) {
            System.out.print("[");
            for (int j = 0; j < M_Resources - 1; j++) {
                System.out.print(Needed_Resources[i][j] + " ");
            }
            System.out.print(Needed_Resources[i][M_Resources - 1] + "]");
        }
        System.out.println(" ");
        for (int i = 0; i < N_Customers; i++) {
            for (int j = 0; j < 1; j++) {
                if (Needed_Resources[i][j] <= Available_Resources[j] && Needed_Resources[i][j + 1] <= Available_Resources[j + 1] && Needed_Resources[i][j + 2] <= Available_Resources[j + 2]) {
                    safestate = true;
                }
            }

        }
        if (safestate) {
            System.out.println("We are safe,no deadlock :D");
        }
        if (!safestate) {
            System.out.println("We are not safe,deadlock waring ");
        }
        System.out.println();
    }

    private boolean isSafeState(int threadNum, int[] request) {
        System.out.print("\n Customer # " + threadNum + " requesting ");
        for (int i = 0; i < M_Resources; i++) {
            System.out.print(request[i] + " ");
        }

        System.out.print("Available = ");
        for (int i = 0; i < M_Resources; i++) {
            System.out.print(Available_Resources[i] + "  ");
        }

        for (int i = 0; i < M_Resources; i++) {
            if (request[i] > Available_Resources[i]) {
                System.err.println("INSUFFICIENT RESOURCES");
                return false;
            }
        }

        boolean[] canFinish = new boolean[N_Customers];
        for (int i = 0; i < N_Customers; i++) {
            canFinish[i] = false;
        }

        int[] avail = new int[M_Resources];
        System.arraycopy(Available_Resources, 0, avail, 0, Available_Resources.length);

        for (int i = 0; i < M_Resources; i++) {
            avail[i] -= request[i];
            Needed_Resources[threadNum][i] -= request[i];
            Allocated_Resources[threadNum][i] += request[i];
        }

        for (int i = 0; i < N_Customers; i++) {
            for (int j = 0; j < N_Customers; j++) {
                if (!canFinish[j]) {
                    boolean temp = true;
                    for (int k = 0; k < M_Resources; k++) {
                        if (Needed_Resources[j][k] > avail[k]) {
                            temp = false;
                        }
                    }
                    if (temp) {
                        canFinish[j] = true;
                        for (int x = 0; x < M_Resources; x++) {
                            avail[x] += Allocated_Resources[j][x];
                        }
                    }
                }
            }
        }

        for (int i = 0; i < M_Resources; i++) {
            Needed_Resources[threadNum][i] += request[i];
            Allocated_Resources[threadNum][i] -= request[i];
        }

        boolean returnValue = true;
        for (int i = 0; i < N_Customers; i++) {
            if (!canFinish[i]) {
                returnValue = false;
                break;
            }
        }

        return returnValue;

    }

    @Override
    public boolean requestResources(int threadNum, int[] request) {
    
             if (!isSafeState(threadNum,request)) {
			return false;
		}
		
		for (int i = 0; i < M_Resources; i++) {
			Available_Resources[i] -= request[i];
			Allocated_Resources[threadNum][i] += request[i];
			Needed_Resources[threadNum][i] = Maximum_Resources[threadNum][i] - Allocated_Resources[threadNum][i];
		}
		
		return true;
        
    }

    @Override
    public void releaseResources(int threadNum, int[] release) {
       System.out.print("\n Customer # " + threadNum + " releasing ");
      for (int i = 0; i < M_Resources; i++) System.out.print(release[i] + " ");
      
      for (int i = 0; i < M_Resources; i++) {
          Available_Resources[i] += release[i];
          Allocated_Resources[threadNum][i] -= release[i];
          Needed_Resources[threadNum][i] = Maximum_Resources[threadNum][i] + Allocated_Resources[threadNum][i];
      }
      
      System.out.print("Available = ");
      for (int i = 0; i < M_Resources; i++)
            System.out.print(Available_Resources[i] + "  ");
      
      System.out.print("Allocated = [");
      for (int i = 0; i < M_Resources; i++)
          System.out.print(Allocated_Resources[threadNum][i] + "  "); 
      System.out.print("]"); 
      
        
        
        
        
    }

}
