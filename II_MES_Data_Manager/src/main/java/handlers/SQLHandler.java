package handlers;

import data.MachineStats;
import data.StatisticMachine;
import data.SubOrder;
import data.UnloadZoneStats;
import logger.ClassLogger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLHandler {
    private final Connection connection;
    private final Logger logger;
    private final int pieceQty;
    private final int unloadZoneQty;
    private final int machineQty;

    SQLHandler(
            Connection connection,
            int pieceQty,
            int unloadZoneQty,
            int machineQty,
            Logger logger
    ) {
        this.connection = connection;
        this.logger = logger;
        this.pieceQty = pieceQty;
        this.machineQty = machineQty;
        this.unloadZoneQty = unloadZoneQty;
    }

    public static SQLHandler createNew(Properties properties, Logger parentLogger) {
        Logger logger = ClassLogger.initLogger(SQLHandler.class.getName(), parentLogger);
        String url = properties.getProperty("DB_URL");
        String user = properties.getProperty("DB_USER");
        String passwd = properties.getProperty("DB_PASSWD");
        String schema = properties.getProperty("DB_SCHEMA");
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, passwd);
            connection.setAutoCommit(true);
            logger.info("Connection with database at \""+url+"\" established");
            PreparedStatement ps = connection.prepareStatement("SET search_path TO " + schema);
            ps.execute();
            int pieceQty = Integer.parseInt(
                    properties.getProperty("PIECE_TYPE_COUNT")
            );
            int unloadZoneQty = Integer.parseInt(
                    properties.getProperty("UNLOAD_ZONE_COUNT")
            );
            int machineQty = Integer.parseInt(
                    properties.getProperty("MACHINE_COUNT")
            );
            return new SQLHandler(
                    connection,
                    pieceQty,
                    unloadZoneQty,
                    machineQty,
                    logger
            );
        } catch (SQLException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Could not establish connection to database", e);
            return null;
        }
    }

    public void setPieces(int count) {
        String insert = "INSERT INTO \"piece\" (\"type\") VALUES (?) ON CONFLICT (type) DO NOTHING";
        try {
            PreparedStatement s = connection.prepareStatement(insert);
            for (int i=1; i <= count; i++) {
                s.setInt(1,i);
                s.addBatch();
            }
            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setMachine(int count) {
        String insert = "INSERT INTO \"machine\" (\"id\") VALUES (?) ON CONFLICT (id) DO NOTHING";
        try {
            PreparedStatement s = connection.prepareStatement(insert);
            for (int i=1; i <= count; i++) {
                s.setInt(1,i);
                s.addBatch();
            }
            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Integer> getStock() {
        String statement = "SELECT piecetype, quantity FROM stocks";
        try {
            PreparedStatement s =
                    connection.prepareStatement(
                            statement,
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY
                    );
            ResultSet resultSet = s.executeQuery();
            HashMap<Integer, Integer> stocks = new HashMap<>(pieceQty);
            int pieceType, qty;
            boolean available = resultSet.first();
            for (int i = 0; i < pieceQty; i++) {
                pieceType = available ? resultSet.getInt("piecetype") : i+1;
                qty = available ? resultSet.getInt("quantity") : 54;
                stocks.put(pieceType, qty);
                available = resultSet.next();
            }
            resultSet.close();
            return stocks;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get stocks", e);
            return null;
        }
    }

    public void updateStock(HashMap<Integer, Integer> stocks) {
        String statement = "INSERT INTO STOCKS VALUES (?, ?) ON CONFLICT (piecetype) DO UPDATE SET QUANTITY = ?";
        try {
            PreparedStatement s = connection.prepareStatement(statement);
            for (Integer type : stocks.keySet()) {
                s.setInt(1, type);
                s.setInt(2, stocks.get(type));
                s.setInt(3, stocks.get(type));
                s.addBatch();
            }
            int[] updates = s.executeBatch();
            for (int i = 0; i < updates.length; i++) {
                if (updates[i] != 1)
                    logger.warning(
                            "Stock entry not updated for piece type "
                                    + stocks.keySet().toArray()[i]
                    );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStock(int pieceType, int stock) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO \"stocks\" VALUES (?, ?) ON CONFLICT (piecetype) DO UPDATE SET QUANTITY = ?"
            );
            ps.setInt(1, pieceType);
            ps.setInt(2, stock);
            ps.setInt(3, stock);
            int qty = ps.executeUpdate();
            if (qty == 1)
                logger.info("Stock updated on DB");
            else
                logger.warning("Could not update stock on DB");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to commit stock statement");
        }
    }

    public void upsertSubOrders(List<SubOrder> subOrders) {
        // TODO insert order into DB
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO \"suborder\" " +
                        "(id, ordernumber, ordertype, state, px, py, quantity, " +
                        "entrytime, starttime, endtime, timeframe, timeleft) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "state = ?, entrytime = ?, starttime = ?, " +
                        "endtime = ?, timeleft = ?"
            );
            for (SubOrder subOrder : subOrders) {
                ps.setInt(1, subOrder.getId());
                ps.setInt(2, subOrder.getOrderNumber());
                ps.setString(3, subOrder.getOrderType());
                String state= subOrder.getState();
                ps.setString(4, state);
                ps.setString(13, state);
                ps.setInt(5, subOrder.getPx());
                ps.setInt(6, subOrder.getPy());
                ps.setInt(7, subOrder.getQuantity());
                LocalDateTime entry = subOrder.getEntryTime();
                ps.setTimestamp(8, entry != null ? Timestamp.valueOf(entry) : null);
                ps.setTimestamp(14, entry != null ? Timestamp.valueOf(entry) : null);
                LocalDateTime start = subOrder.getStartTime();
                ps.setTimestamp(9, start != null ? Timestamp.valueOf(start) : null);
                ps.setTimestamp(15, start != null ? Timestamp.valueOf(start) : null);
                LocalDateTime end = subOrder.getEndTime();
                ps.setTimestamp(10, end != null ? Timestamp.valueOf(end) : null);
                ps.setTimestamp(16, end != null ? Timestamp.valueOf(end) : null);
                ps.setTimestamp(11, Timestamp.valueOf(subOrder.getMaxFinishingTime()));
                ps.setLong(12, subOrder.getTimeLeft());
                ps.setLong(17, subOrder.getTimeLeft());
                ps.addBatch();
            }
            int[] qtys = ps.executeBatch();
            for (int qty : qtys) {
                if (qty != 1)
                    logger.warning("Failed to insert/update suborder in DB");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to insert/update subOrder in DB", e);
        }
    }

    public List<SubOrder> getSubOrders() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT ordernumber, ordertype,\n" +
                            "count(*) FILTER (WHERE state = 'waiting') AS \"waiting\",\n" +
                            "count(*) FILTER (WHERE state = 'current') AS \"current\",\n" +
                            "count(*) FILTER (WHERE state = 'finished') AS \"finished\",\n" +
                            "CASE \n" +
                            "WHEN count(*) FILTER (WHERE state = 'current') = 0 AND count(*) FILTER (WHERE state = 'finished') = 0 THEN 'waiting'\n" +
                            "WHEN count(*) FILTER (WHERE state = 'waiting') = 0 AND count(*) FILTER (WHERE state = 'current') = 0 THEN 'finished'\n" +
                            "ELSE 'current'" +
                            "END AS \"state\",\n" +
                            "min(entrytime) AS \"entrytime\",\n" +
                            "min(starttime ) AS \"starttime\",\n" +
                            "max(endtime) AS \"endtime\",\n" +
                            "min(timeleft) AS \"timeleft\"\n" +
                            "FROM suborder\n" +
                            "GROUP BY ordernumber, quantity, ordertype"
            );
            ResultSet resultSet = ps.executeQuery();
            ArrayList<SubOrder> subOrders = new ArrayList<>();
            while (resultSet.next()) {
                Timestamp startTime = resultSet.getTimestamp("starttime");
                Timestamp endTime = resultSet.getTimestamp("endtime");
                String state = resultSet.getString("state");
                SubOrder subOrder = new SubOrder(
                        resultSet.getInt("ordernumber"),
                        resultSet.getString("ordertype"),
                        state,
                        resultSet.getTimestamp("entrytime").toLocalDateTime(),
                        startTime != null ? startTime.toLocalDateTime() : null,
                        endTime != null && state.equals("finished") ? endTime.toLocalDateTime() : null,
                        resultSet.getInt("waiting"),
                        resultSet.getInt("current"),
                        resultSet.getInt("finished"),
                        !state.equals("waiting") ? resultSet.getLong("timeleft") : null
                );
                subOrders.add(subOrder);
            }
            return subOrders;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Could not query existing SubOrders from DB", e);
            return null;
        }
    }

    public List<SubOrder> getStateFilteredOrders(String stateFilter) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT ordernumber, ordertype, state, px, py, " +
                            "quantity, timeframe, entrytime, id, " +
                            "starttime, timeleft " +
                            "FROM suborder WHERE state = ?"
            );
            ps.setString(1, stateFilter);
            ResultSet resultSet = ps.executeQuery();
            ArrayList<SubOrder> subOrders = new ArrayList<>();
            while (resultSet.next()) {
                Timestamp startTime = resultSet.getTimestamp("starttime");
                SubOrder subOrder = new SubOrder(
                        resultSet.getInt("ordernumber"),
                        resultSet.getString("ordertype"),
                        resultSet.getString("state"),
                        resultSet.getInt("px"),
                        resultSet.getInt("py"),
                        resultSet.getInt("quantity"),
                        resultSet.getTimestamp("timeframe").toLocalDateTime(),
                        resultSet.getTimestamp("entrytime").toLocalDateTime(),
                        startTime != null ? startTime.toLocalDateTime() : null,
                        resultSet.getLong("timeleft"),
                        resultSet.getInt("id")
                );
                subOrders.add(subOrder);
            }
            return subOrders;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error trying to query waiting orders", e);
            return null;
        }
    }

    public Vector<Vector<Integer>> getUnloadZoneStats() {
        String statement = "SELECT unloadzone, piecetype, quantity FROM delivered_pieces";
        try {
            PreparedStatement s =
                    connection.prepareStatement(
                            statement,
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY
                    );
            ResultSet resultSet = s.executeQuery();
            Vector<Vector<Integer>> unloadZones = new Vector<>(unloadZoneQty);
            for (int i = 0; i < unloadZoneQty; i++) {
                unloadZones.add(i, new Vector<>(pieceQty));
                for (int j = 0; j < pieceQty; j++) {
                    unloadZones.get(i).add(0);
                }
            }
            int unloadZone, pieceType, qty;
            boolean available = resultSet.first();
            for (int i = 0; i < unloadZoneQty; i++) {
                for (int j = 0; j < pieceQty; j++) {
                    unloadZone = available ? resultSet.getInt("unloadzone") : i+1;
                    pieceType = available ? resultSet.getInt("piecetype") : j+1;
                    qty = available ? resultSet.getInt("quantity") : 0;
                    unloadZones.get(unloadZone-1).set(pieceType-1, qty);
                    available = resultSet.next();
                }
            }
            resultSet.close();
            return unloadZones;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get stocks", e);
            return null;
        }
    }

    public void upsertUnloadZones(UnloadZoneStats unloadZoneStats) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO delivered_pieces " +
                            "(unloadzone, piecetype, quantity) " +
                            "VALUES (?, ?, ?) ON CONFLICT (unloadzone, piecetype) " +
                            "DO UPDATE SET quantity = ?"
            );
            for (int i = 0; i < unloadZoneStats.getUnloadZoneCount(); i++) {
                for (int j = 0; j < unloadZoneStats.getPiecetypeCount(); j++) {
                    int quantity = unloadZoneStats.getStat(i,j);
                    ps.setInt(1, i+1);
                    ps.setInt(2, j+1);
                    ps.setInt(3, quantity);
                    ps.setInt(4, quantity);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to upsert unload zone statistics", e);
        }
    }

    public Vector<Vector<Integer>> getMachineStats() {
        String statement = "SELECT machineid, piecetype, quantity FROM processed_pieces";
        try {
            PreparedStatement s =
                    connection.prepareStatement(
                            statement,
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY
                    );
            ResultSet resultSet = s.executeQuery();
            Vector<Vector<Integer>> machines = new Vector<>(machineQty);
            for (int i = 0; i < machineQty; i++) {
                machines.add(i, new Vector<>(pieceQty));
                for (int j = 0; j < pieceQty; j++) {
                    machines.get(i).add(0);
                }
            }
            int machine, pieceType, qty;
            boolean available = resultSet.first();
            for (int i = 0; i < machineQty; i++) {
                for (int j = 0; j < pieceQty; j++) {
                    machine = available ? resultSet.getInt("machineid") : i+1;
                    pieceType = available ? resultSet.getInt("piecetype") : j+1;
                    qty = available ? resultSet.getInt("quantity") : 0;
                    machines.get(machine-1).set(pieceType-1, qty);
                    available = resultSet.next();
                }
            }
            resultSet.close();
            return machines;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get machine statistics", e);
            return null;
        }
    }

    public Vector<Long> getMachineWorkTimeStats() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT machineid, worktime FROM machine_worktime ",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            );
            ResultSet resultSet = ps.executeQuery();
            Vector<Long> machineWorkTimes = new Vector<>(machineQty);
            long workTime;
            int machineId;
            boolean available = resultSet.first();
            for (int i = 0; i < machineQty; i++) {
                machineId = available ? resultSet.getInt("machineid") : i+1;
                workTime = available ? resultSet.getLong("worktime") : 0L;
                machineWorkTimes.add(machineId-1, workTime);
                available = resultSet.next();
            }
            return machineWorkTimes;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void upsertMachineStats(MachineStats machineStats) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO processed_pieces " +
                            "(machineid, piecetype, quantity) " +
                            "VALUES (?, ?, ?) ON CONFLICT (machineid, piecetype) " +
                            "DO UPDATE SET quantity = ?"
            );
            PreparedStatement ps2 = connection.prepareStatement(
                    "INSERT INTO machine_worktime " +
                            "(machineid, worktime) " +
                            "VALUES (?, ?) ON CONFLICT (machineid) " +
                            "DO UPDATE SET worktime = ?"
            );
            for (int i = 0; i < machineStats.getMachineCount(); i++) {
                for (int j = 0; j < machineStats.getPiecetypeCount(); j++) {
                    int quantity = machineStats.getStat(i,j);
                    ps.setInt(1, i+1);
                    ps.setInt(2, j+1);
                    ps.setInt(3, quantity);
                    ps.setInt(4, quantity);
                    ps.addBatch();
                }
                long workTime = machineStats.getWorkTimeStat(i);
                ps2.setInt(1, i+1);
                ps2.setLong(2, workTime);
                ps2.setLong(3, workTime);
                ps2.addBatch();
            }
            ps.executeBatch();
            ps2.executeBatch();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to upsert machine statistics", e);
        }
    }

    public Integer getLastId() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT max(id) AS id FROM suborder"
            );
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

}
