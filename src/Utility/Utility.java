package Utility;

import java.sql.*;;
import java.sql.ResultSet;
import Database.*;
import Session.Manager;
import java.io.Console;
import java.time.LocalDate;

public class Utility{
    public static final int OPENTIME = 9;    // time for opening the market
    public static final int CLOSETIME = 17;  // time for closing the market


    // parameters for connecting to the MySQL server
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String HOST = "jdbc:mysql://localhost/xuhui_sunDB";
    public static final String USER = "root";
    public static final String PWD = "950802cherry";

    public static Connection connection;
    public static Statement statement;

    public static String date;   // system date
    public static Boolean marketState = false; // open or closed

    public static double interestRate = 0.03;

    public static Boolean logout = false; // logout flag

    public static ResultSet sql_query(String QUERY){
        Connection connection = Utility.connection;
        Statement statement = Utility.statement;
        ResultSet resultSet = null;
        try{
            // find the username and password pair entity
            resultSet =  statement.executeQuery(QUERY);
            return resultSet;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        //finally {
        //    try {
        //        if(statement != null)
        //            statement.close();
        //    } catch(Exception e){
        //        e.printStackTrace();
        //    }
        //}
        return resultSet;
    }

    public static void sql_update(String UPDATE){
        Connection connection = Utility.connection;
        Statement statement = null;
        try{
            // find the username and password pair entity
            statement = connection.createStatement();
            statement.executeUpdate(UPDATE);
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if(statement != null)
                    statement.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void open_market(){
        marketState = true;
    }

    public static void close_market(){
        marketState = false;
    }

    public static Boolean market_is_open(){
        return marketState;
    }

    // public static void set_current_date(){
    //     date = LocalDate.now();
    // }


    public static void set_date(String d){
        // int pass = Integer.parseInt(d.substring(4,6))-Integer.parseInt(date.substring(4,6));
        // if(pass>1){
        //     System.out.println("cannot fast forward more than 1 month.\n");
        //     return;
        // }else if(pass==1){
        //     int year = Integer.parseInt(date.substring(0,4));
        //     int month = Integer.parseInt(date.substring(4,6));
        //
        //     int daysInMonth;
        //     switch (month) {
        //         case 1: // fall through
        //         case 3: // fall through
        //         case 5: // fall through
        //         case 7: // fall through
        //         case 8: // fall through
        //         case 10: // fall through
        //         case 12:
        //             daysInMonth = 31;
        //             break;
        //         case 2:
        //             if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
        //                 daysInMonth = 29;
        //             } else {
        //                 daysInMonth = 28;
        //             }
        //             break;
        //         default:
        //             // returns 30 even for nonexistant months
        //             daysInMonth = 30;
        //     }
        //     date = date.substring(0,6) + String.valueOf(daysInMonth);
        //     store_date();
        //     Manager M = new Manager();
        //     M.add_interest();
        //     M.delete_transaction();
        // }
        int oldYear = Integer.parseInt(date.substring(0,4));
        int oldMonth = Integer.parseInt(date.substring(4,6));
        int oldDay = Integer.parseInt(date.substring(6,8));

        int newYear = Integer.parseInt(d.substring(0,4));
        int newMonth = Integer.parseInt(d.substring(4,6));
        int newDay = Integer.parseInt(d.substring(6,8));


        LocalDate old = LocalDate.of(oldYear, oldMonth, oldDay);
        LocalDate new_date = LocalDate.of(newYear, newMonth, newDay);


        while(old.isBefore(new_date)){
            int year = old.getYear();
            int month = old.getMonthValue();
            int day = old.getDayOfMonth();

            String temp = (new Integer(year)).toString() + (new Integer(month)).toString() + (new Integer(day)).toString();

            if(day == old.lengthOfMonth()){
                Manager M = new Manager();
                M.add_interest();
                M.delete_transaction();
                System.out.println("Interests added for this month. Date:" + temp);
                System.out.println("Transactions of this month deleted. Date:" + temp);
            }


            String QUERY =  "SELECT * " +
                            "FROM ActorsStockInfo";

            ResultSet resultSet = Utility.sql_query(QUERY);

            String res = "";
            try{
                while(resultSet.next()){
                    String id = resultSet.getString("ACTORID");
                    double price = resultSet.getDouble("CURRENTPRICE");

                    Statement saved = Utility.statement;
                    Utility.statement = Utility.connection.createStatement();

                    StockHistory_DB.record_history(temp, id, price);

                    Utility.statement.close();
                    Utility.statement = saved;
                }
            } catch(Exception e){
                e.printStackTrace();
            }

            old = old.plusDays(1);
        }

        date = d;
        store_date();
    }


    public static void load_date(){
        date = Date_DB.load_date();
    }

    public static void store_date(){
        Date_DB.store_date(date);
    }



    public static Boolean sign_up(){
        Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }

        String username = c.readLine("Username: ");

        if(Customer_DB.username_exist(username)){
            System.out.println("Username already exist.\n");
            return false;
        }

        String taxID = c.readLine("TaxID: ");

        if(Customer_DB.taxid_exist(taxID)){
            System.out.println("TaxID already exist.\n");
            return false;
        }

        String ssn = c.readLine("ssn: ");

        if(Customer_DB.ssn_exist(ssn)){
            System.out.println("SSN already exist.\n");
            return false;
        }

        String password = c.readLine("Password: ");
        String name = c.readLine("Name: ");
        String address = c.readLine("Address: ");
        String STATE = c.readLine("STATE: ");
        String phone = c.readLine("Phone: ");
        String email = c.readLine("Email: ");

        Customer_DB.insert_new_user(name, username, password, address, STATE, phone, email, taxID, ssn);
        return true;
    }

}
