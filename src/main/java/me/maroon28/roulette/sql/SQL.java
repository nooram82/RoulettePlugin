package me.maroon28.roulette.sql;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQL {

    private String query;
    private final List<Object> values = new ArrayList<>();
    private final Connection conn = null;

    private ResultCallback success;
    private ErrorCallback error;
    private boolean isValid = true; // asume its true, unless another method says otherwise
    private long delay = 0L;

    private QueryTypes queryType = QueryTypes.execute; // by default execute for select, in the weird case theres select and update in the same query use this

    public SQL(String query) {
        this(query, false);
    }

    public SQL(String query, Object quickValue) {
        this(query, false);
        values.add(quickValue);
    }

    public SQL(String query, Object quickValue, Boolean debug) {
        this(query, debug);
        values.add(quickValue);
    }

    public SQL(String query, Boolean debug) {
        String patternString = "\\?\\*\\d+"; // finds how many ?*X there are
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(query);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String match = matcher.group();
            matcher.appendReplacement(sb, replaceQuestionMarks(match));
        }
        matcher.appendTail(sb);

        this.query = sb.toString()
                // do replace all in case its a nested query
                .replaceAll(":SA ", "select * ")
                .replaceAll(":S ", "select ")
                .replaceAll(":F ", "from ")
                .replaceAll(":W ", "where ")
                .replaceAll(":I ", "insert into ")
                .replaceAll(":V ", "values ")
                .replaceAll(":U ", "update ")
                .replaceAll(":D ", "delete from ")
                .replaceAll(":NN", "is not null")
                .replaceAll(":N", "is null ")
                .replaceAll(":B ", "between ")
                .replaceAll(":WN ", "when ")
                .replaceAll(":OB ", "order by ")
                .replaceAll(" a ", " and ")
                .replaceAll(" s ", " set ")
                .replaceAll(":P ", "players ")
                .replaceAll(":L ", "limit ")
                .replaceAll(" :\\?", " = ?");

        if (debug) System.out.println("query = " + this.query);
        try {
            //  this.conn = SQLManager.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String replaceQuestionMarks(String match) {
        int amount = Integer.parseInt(match.substring(2));
        return "(" + String.join(", ", Collections.nCopies(amount, "?")) + ")";
    }

    public SQL setType(QueryTypes type) {
        queryType = type;
        return this;
    }

    public SQL addValue(Object value) {
        this.values.add(value);
        return this;
    }

    public SQL addValues(Object... values) {
        //this.values.addAll(values);
        this.values.addAll(Arrays.asList(values));
        return this;
    }

    public SQL isValid(boolean toAsk) {
        this.isValid = toAsk;
        System.out.println("isValid = " + isValid);

        return this;
    }

    public SQL delay(long delay) {
        this.delay = delay;
        return this;
    }


    public static class ResultSetData {
        public List<Map<String, Object>> rows = new ArrayList<>();
        private int index = -1;

        public void handleResultSet(ResultSet rs) throws SQLException {
            ResultSetMetaData metaData;
            try {
                metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                //System.out.println("columnCount = " + columnCount);

                while (rs.next()) {
                    Map<String, Object> rowMap = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        rowMap.put(metaData.getColumnName(i), rs.getObject(i));
                        //System.out.println("metaData.getColumnName(i) = " + metaData.getColumnName(i));
                        //System.out.println("rs.getObject(i) = " + rs.getObject(i));
                    }

                    rows.add(rowMap);
                }

                //System.out.println("rows = " + rows);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public boolean next() {
            return !(rows.isEmpty()) && index + 1 < rows.size() && rows.get(++index) != null;
        }

        @Nullable
        public String getString(String toGet) {
            try {
                return (String) rows.get(index).get(toGet);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public byte @Nullable [] getBytes(String toGet) {
            try {
                return ((byte[]) rows.get(index).get(toGet));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Nullable
        public Integer getInteger(String toGet) {
            try {
                return ((Integer) rows.get(index).get(toGet));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Nullable
        public Integer getInt(String toGet) {
            return getInteger(toGet);
        }

        @Nullable
        public BigInteger getBigInt(String toGet) {
            try {
                return ((BigInteger) rows.get(index).get(toGet));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void setIndex(Integer newIndex) {
            index = newIndex;
        }

        @Override
        public String toString() {
            return "ResultSetData{" +
                    "rows=" + rows +
                    ", index=" + index +
                    '}';
        }
    }

    /*
        public CompletableFuture<Void> execute() {
        if (!isValid) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> executeCommon(false));
    }
     */
    public void execute() {
        if (!isValid) {
            CompletableFuture.completedFuture(null);
            return;
        }
        if (delay == 0)
            CompletableFuture.runAsync(() -> executeCommon(false));
        else {
   //         Bukkit.getScheduler().runTaskLaterAsynchronously(Roulette.getInstance(), () ->
   //                 CompletableFuture.runAsync(() -> executeCommon(false)), delay);
        }
    }

    public CompletableFuture<ResultSetData> executeAndGet() {
        if (!isValid) return CompletableFuture.completedFuture(null);

        Executor delayedExecutor = CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS);

        return CompletableFuture.supplyAsync(() -> executeCommon(true), delayedExecutor);
    }

    private ResultSetData executeCommon(boolean getResults) {
        ResultSetData data = new ResultSetData();

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            if (!values.isEmpty()) {
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i) instanceof UUID) values.set(i, values.get(i).toString());
                    stmt.setObject(i + 1, values.get(i));
                }
            }

            if (query.toLowerCase().contains("select") && queryType.equals(QueryTypes.execute)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    data.handleResultSet(rs);
                    if (success != null) success.onSuccess(data);
                    //return getResults ? rs : null;
                }
            } else {
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    data.handleResultSet(rs);
                    if (success != null) success.onSuccess(data);
                    //return getResults ? rs : null;
                }
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            if (error != null) error.onError(e);
            else e.printStackTrace();
            return null;
        }
        return getResults ? data : null;
    }

    public SQL onError(ErrorCallback callback) {
        this.error = callback;
        return this;
    }

    public SQL onSuccess(ResultCallback callback) {
        this.success = callback;
        return this;
    }

    public interface ResultCallback {
        void onSuccess(ResultSetData data) throws SQLException, IOException, ClassNotFoundException;
    }

    public interface ErrorCallback {
        void onError(Exception e);
    }

    public interface SuccessWithoutResult {
        void onSuccess();
    }

    public static enum QueryTypes {
        update,
        execute
    }
}