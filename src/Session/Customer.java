package Session;

import Utility.*;
import java.sql.*;
import Database.*;
import java.io.Console;

public class Customer extends Session{
    private String marketAccountID;
    // private String stockAccountID;
    private String taxID;

    // Override virtual function from super class
    @Override
    public void display_operations(){
        String options = "Please enter the number. Options:\n";
        options += "1:  Deposit.\n";
        options += "2:  Withdrawl.\n";
        options += "3:  Buy.\n";
        options += "4:  Sell.\n";
        options += "5:  Show Balance.\n";
        options += "6:  Show Transaction history.\n";
        options += "7:  List Stock Info.\n";
        options += "8:  List Moive Info. \n";
        options += "9:  List Top Movie.\n";
        options += "10: List Movie Revie.\n";
        options += "11: Exit.\n";

        System.out.println(options);
    }

    @Override
    public void process_operations(String request){
        Utility.load_date();
        switch (request) {
            case "1":   this.deposit();
                        break;
            case "2":   this.withdrawl();
                        break;
            case "3":   this.buy();
                        break;
            case "4":   this.sell();
                        break;
            case "5":   this.show_balance();
                        break;
            case "6":   this.show_transaction();
                        break;
            case "7":   this.list_actor_stock_info();
                        break;
            case "8":   this.list_movie_info();
                        break;
            case "9":   this.list_top_movie();
                        break;
            case "10":  this.list_review();
                        break;
            case "11":  Utility.logout = true;
                        break;
            default:    System.out.println("Wrong input, please try again.\n");
        }
    }

    @Override
    public Boolean verify_login(String username, String password){
        Connection connection = Utility.connection;
        Statement statement = null;
        try{
            // find the username and password pair entity and get the tax id
            taxID = Customer_DB.get_tax_id(username, password);

            // if tax id does not exist
            if(taxID.equals(new String("-1"))){
                return false;
            }

            marketAccountID = AccountMarket_DB.get_market_account_id(taxID);
            // stockAccountID = AccountStock_DB.get_stock_account_id(taxID);

            return true;
        } finally {
            try{
                if(statement != null){
                    statement.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // operation deltails
    public void deposit(){
        if(Utility.market_is_open() == false){
            System.out.println("Market Closed.\n");
            return;
        }

        Console c = System.console();
        if (c == null) {
            System.err.println("No console.\n");
            System.exit(1);
        }

        String amount = c.readLine("Please enter the amount:");

        if(Utility.market_is_open() == false){
            System.out.println("Market Closed.\n");
            return;
        }

        AccountMarket_DB.add_balance(marketAccountID, Double.parseDouble(amount));

        Utility.load_date();

        double balance = AccountMarket_DB.get_account_balance(marketAccountID);
        MarketTransaction_DB.record_transaction(Utility.date, taxID, Double.parseDouble(amount), balance);
    }

    public void withdrawl(){
        if(Utility.market_is_open() == false){
            System.out.println("Market Closed.\n");
            return;
        }

        Console c = System.console();
        if (c == null) {
            System.err.println("No console.\n");
            System.exit(1);
        }

        String temp = c.readLine("Please enter the amount:");
        double amount = Double.parseDouble(temp);
        double balance = AccountMarket_DB.get_account_balance(marketAccountID);

        if(balance < amount){
            System.out.println("Request denied! Not enough balance!\n");
            return;
        }

        if(Utility.market_is_open() == false){
            System.out.println("Market Closed.\n");
            return;
        }

        AccountMarket_DB.add_balance(marketAccountID, -amount);

        Utility.load_date();

        balance = AccountMarket_DB.get_account_balance(marketAccountID);
        MarketTransaction_DB.record_transaction(Utility.date, taxID, -amount, balance);
    }

    public void buy(){

        if(Utility.check_active(taxID) == false){
            System.out.println("Not enough balance. Account State: Inactive.\n");
            return;
        }

        if(Utility.market_is_open() == false){
            System.out.println("Market Closed.\n");
            return;
        }

        Console c = System.console();
        if (c == null) {
            System.err.println("No console.\n");
            System.exit(1);
        }

        String actorID = c.readLine("Stock you want to buy:");
        if(actorID.length() != 3){
            System.out.println("Invalid Stock symbol\n");
            return;
        }

        double price = ActorStockInfo_DB.get_price(actorID);
        if(price == -1.0){
            System.out.println("No such stock!\n");
            return;
        }

        String temp = c.readLine("Amount you want to buy:");
        int amount = Integer.parseInt(temp);

        double spent = amount * price + 20;

        double balance = AccountMarket_DB.get_account_balance(marketAccountID);
        if(spent > balance){
            System.out.println("Request denied! Not enough balance\n");
            return;
        }

        if(Utility.market_is_open() == false){
            System.out.println("Market Closed.\n");
            return;
        }

        Utility.load_date();

        AccountStock_DB.add_shares(taxID, amount, actorID, price);
        AccountMarket_DB.add_balance(marketAccountID, -spent);

        StockTransaction_DB.record_transaction(Utility.date, taxID, actorID, price, amount, 0);
    }


    public void sell(){

        if(Utility.check_active(taxID) == false){
            System.out.println("Not enough balance. Account State: Inactive.\n");
            return;
        }

        if(Utility.market_is_open() == false){
            System.out.println("Market Closed.\n");
            return;
        }

        Console c = System.console();
        if (c == null) {
            System.err.println("No console.\n");
            System.exit(1);
        }

        String actorID = c.readLine("Stock you want to sell: ");
        if(actorID.length() != 3){
            System.out.println("Invalid Stock symbol!\n");
            return;
        }

        String info = AccountStock_DB.get_shares_info(taxID, actorID);
        System.out.println("Current shares summary:");
        System.out.println(info);

        String temp = c.readLine("The bought price for the shares you want to sell: ");
        double price = Double.parseDouble(temp);
        int shares = AccountStock_DB.get_shares(taxID, actorID, price);
        if(shares == -1){
            System.out.println("No such bought price!\n");
            return;
        }

        double curPrice = ActorStockInfo_DB.get_price(actorID);
        if(price == -1.0){
            System.out.println("No such stock!\n");
            return;
        }

        temp = c.readLine("Amount you want to sell:");
        int amount = Integer.parseInt(temp);

        if(amount > shares){
            System.out.println("Not enough shares. Request denied.\n");
            return;
        }

        if(Utility.market_is_open() == false){
            System.out.println("Market Closed.\n");
            return;
        }


        Utility.load_date();

        AccountStock_DB.add_shares(taxID, -amount, actorID, price);
        AccountMarket_DB.add_balance(marketAccountID, price*amount-20);

        double profit = curPrice*amount - price*amount - 20;
        System.out.print("selling price:");
        System.out.println(curPrice);
        System.out.print("bought price:");
        System.out.println(price);
        System.out.print("amount:");
        System.out.println(amount);
        System.out.print("profit:");
        System.out.println(profit);



        StockTransaction_DB.record_transaction(Utility.date, taxID, actorID, price, -amount, profit);
    }

    public void show_balance(){
        double balance = AccountMarket_DB.get_account_balance(marketAccountID);
        System.out.println("Current balance: " + (new Double(balance)).toString());
    }

    public void show_transaction(){
        String marketTransactions = MarketTransaction_DB.get_transactions(taxID);
        String stockTransactions = StockTransaction_DB.get_transactions(taxID);
        String interestTransactions = InterestTransaction_DB.get_transactions(taxID);
        System.out.println("Transactions:");
        System.out.println(marketTransactions);
        System.out.println(stockTransactions);
        System.out.println(interestTransactions);
    }

    public void list_actor_stock_info(){
        String res = ActorStockInfo_DB.list_all();
        System.out.println("All Actor and Stock Info:\n" + res);
    }

    public void list_movie_info(){
        Console c = System.console();
        if (c == null) {
            System.err.println("No console.\n");
            System.exit(1);
        }

        String title = c.readLine("Please enter movie title:");
        // String movieInfo = MovieXMLParser.display_info(title);
        // if(movieInfo.equals("\n"))
        //     System.out.println("No such movie.\n");
        // else
        //     System.out.println(movieInfo);

        String movieInfo = Movies_DB.get_movie(title);
        if(movieInfo.equals(""))
            System.out.println("No such movie.\n");
        else
            System.out.println(movieInfo);
    }

    public void list_top_movie(){
        Console c = System.console();
        if (c == null) {
            System.err.println("No console.\n");
            System.exit(1);
        }

        String time = c.readLine("Please enter time interval in following format(YYYY-YYYY):");
        if(time.length()!=9 || time.charAt(4)!='-') {
            System.out.println("invalid input.\n");
            return;
        }
        // String topMovie = MovieXMLParser.top_movie(time);
        // if(topMovie.equals("\n"))
        //     System.out.println("No such movie.\n");
        // else
        //     System.out.println(topMovie);
        String topMovie = Movies_DB.top_movies(time);
        if(topMovie.equals(""))
            System.out.println("No such movie.\n");
        else
            System.out.println(topMovie);
    }

    public void list_review(){
        Console c = System.console();
        if (c == null) {
            System.err.println("No console.\n");
            System.exit(1);
        }

        String title = c.readLine("Please enter movie title:");
        // String review = MovieXMLParser.display_review(title);
        // if(review.equals("\n"))
        //     System.out.println("No such movie.\n");
        // else
        //     System.out.println(review);
        String review = Movies_DB.display_review(title);
        if(review.equals(""))
            System.out.println("No such movie.\n");
        else
            System.out.println(review);
    }
}
