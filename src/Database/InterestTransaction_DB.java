package Database;

import Utility.Utility;
import java.sql.*;


public class InterestTransaction_DB {
    public static Boolean empty(){
        String QUERY =  "SELECT * " +
                        "FROM InterestTransactions ";

        ResultSet resultSet = Utility.sql_query(QUERY);

        try{
            if(!resultSet.next()){
                return true;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public static void record_transaction(String date, String customerTAXID, String managerTAXID, double interest, double balance){
        String UPDATE = "INSERT INTO InterestTransactions " +
                        "VALUES(" + "'" + date + "'" + ","
                                + "'" + customerTAXID + "'" + ","
                                + "'" + managerTAXID + "'" + ","
                                + interest + ","
                                + balance + ")";

        Utility.sql_update(UPDATE);
    }

    public static double get_interest(String taxID){
        String QUERY =  "SELECT * " +
                        "FROM InterestTransactions " +
                        "WHERE CustomerTAXID = " + "'" + taxID + "'";

        ResultSet resultSet = Utility.sql_query(QUERY);

        double res = 0.0;
        try{
            if(!resultSet.next()){
                return 0.0;
            }

            res = resultSet.getDouble("interest");

            // if(resultSet.next()){
            //     System.out.println("Invalid data in database: multiple interest info");
            //     System.exit(2);
            // }

        }
        catch(Exception e){
            e.printStackTrace();
        }

        return res;
    }

    public static void delete_transaction(){
        String UPDATE = "DELETE " +
                        "FROM InterestTransactions ";
        Utility.sql_update(UPDATE);
    }

    public static String get_transactions(String taxID){
        String QUERY =  "SELECT * " +
                        "FROM InterestTransactions " +
                        "WHERE CustomerTAXID = " + "'" + taxID + "'";

        ResultSet resultSet = Utility.sql_query(QUERY);

        String res = "";

        try{
            while(resultSet.next()){
                String date = resultSet.getString("date");
                String customerTAXID = resultSet.getString("customerTAXID");
                String managerTAXID = resultSet.getString("managerTAXID");
                double interest = resultSet.getDouble("interest");
                double balance = resultSet.getDouble("balance");


                res += "Date: " + date;
                res += ", Transaction type: " + "Accrue-Interest";
                res += ", Customer TaxID: "  + customerTAXID;
                res += ", Manager TaxID: " + managerTAXID;
                res += ", Interest: " + (new Double(interest)).toString();
                res += ", Balance: " + (new Double(balance)).toString();
                res += "\n";
            }
        } catch(Exception e){
            e.printStackTrace();
        }

        return res;
    }
}
