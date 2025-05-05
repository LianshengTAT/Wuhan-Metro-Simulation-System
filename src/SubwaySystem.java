import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * 地铁系统核心类，负责解析地铁数据、实现各种地铁查询功能，如获取换乘站、查找邻近站点、路径规划和票价计算等。
 */
class SubwaySystem {
    // 存储站点与线路的映射（站点名 -> 线路集合）
    Map<String, Set<String>> stationLines = new HashMap<>();
    // 邻接表存储连接关系（站点名 -> 连接列表）
    Map<String, List<Connection>> graph = new HashMap<>();
    // 线路数据缓存（线路名 -> 站点列表）
    Map<String, List<String>> lineStations = new HashMap<>();

    /**
     * 解析地铁数据文件，将文件中的站点、线路和距离信息存储到相应的数据结构中。
     * 
     * @param filename 地铁数据文件的名称
     * @throws IOException 如果文件读取过程中出现错误
     */
    public void parseData(String filename) throws IOException {
        String currentLine = null;
        List<String> currentStations = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                // 检测线路标题行（如"1号线站点间距"）
                if (line.contains("号线站点间距")) {
                    if (currentLine != null) {
                        lineStations.put(currentLine, currentStations);
                    }
                    currentLine = line.split("号")[0] + "号线";
                    currentStations = new ArrayList<>();
                    br.readLine(); // 跳过表头
                    br.readLine(); // 跳过空行
                    continue;
                }

                // 解析站点数据
                String[] parts = line.split("\t+");
                if (parts.length >= 2) {
                    String[] stations = parts[0].split("---");
                    if (stations.length == 2) {
                        String s1 = stations[0].trim();
                        String s2 = stations[1].trim();
                        double distance = Double.parseDouble(parts[1]);

                        // 更新站点线路信息
                        addStationToLine(s1, currentLine);
                        addStationToLine(s2, currentLine);

                        // 构建邻接表
                        addConnection(s1, s2, currentLine, distance);
                        addConnection(s2, s1, currentLine, distance);

                        // 记录线路顺序
                        if (!currentStations.contains(s1))
                            currentStations.add(s1);
                        if (!currentStations.contains(s2))
                            currentStations.add(s2);
                    }
                }
            }
            if (currentLine != null) {
                lineStations.put(currentLine, currentStations);
            }
        }
    }

    /**
     * 将站点添加到指定线路中。
     * 
     * @param station 站点名称
     * @param line    线路名称
     */
    private void addStationToLine(String station, String line) {
        stationLines.putIfAbsent(station, new HashSet<>());
        stationLines.get(station).add(line);
    }

    /**
     * 在邻接表中添加站点之间的连接关系。
     * 
     * @param from     起始站点名称
     * @param to       目标站点名称
     * @param line     所属线路名称
     * @param distance 站点之间的距离
     */
    private void addConnection(String from, String to, String line, double distance) {
        graph.putIfAbsent(from, new ArrayList<>());
        graph.get(from).add(new Connection(to, line, distance));
    }

    /**
     * 获取所有换乘站及其所在线路。
     * 
     * @return 一个映射，键为换乘站名称，值为该站点所在的线路集合
     */
    public Map<String, Set<String>> getTransferStations() {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : stationLines.entrySet()) {
            if (entry.getValue().size() >= 2) {
                result.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * 获取指定站点周围距离小于指定值的所有站点。
     * 
     * @param station     指定的站点名称
     * @param maxDistance 最大距离
     * @return 一个列表，包含符合条件的站点信息，每个元素是一个字符串数组，包含站点名称、线路名称和距离
     */
    public List<String[]> getNearbyStations(String station, double maxDistance) {
        List<String[]> result = new ArrayList<>();
        if (!stationLines.containsKey(station))
            return result;

        // 遍历所有相关线路
        for (String line : stationLines.get(station)) {
            List<String> stations = lineStations.get(line);
            int index = stations.indexOf(station);

            // 向左遍历
            double accumulated = 0;
            for (int i = index - 1; i >= 0; i--) {
                accumulated += getDistance(stations.get(i + 1), stations.get(i), line);
                if (accumulated > maxDistance)
                    break;
                result.add(new String[] { stations.get(i), line, String.valueOf(accumulated) });
            }

            // 向右遍历
            accumulated = 0;
            for (int i = index + 1; i < stations.size(); i++) {
                accumulated += getDistance(stations.get(i - 1), stations.get(i), line);
                if (accumulated > maxDistance)
                    break;
                result.add(new String[] { stations.get(i), line, String.valueOf(accumulated) });
            }
        }
        return result;
    }

    /**
     * 获取两个站点之间的距离。
     * 
     * @param s1   第一个站点名称
     * @param s2   第二个站点名称
     * @param line 所属线路名称
     * @return 两个站点之间的距离，如果未找到则返回0
     */
    private double getDistance(String s1, String s2, String line) {
        for (Connection conn : graph.get(s1)) {
            if (conn.target.equals(s2) && conn.line.equals(line)) {
                return conn.distance;
            }
        }
        return 0;
    }

    /**
     * 使用Dijkstra算法计算两个站点之间的最短路径。
     * 
     * @param start 起始站点名称
     * @param end   目标站点名称
     * @return 包含最短路径信息的PathResult对象
     */
    public PathResult shortestPath(String start, String end) {
        // 初始化数据结构
        PriorityQueue<Node> pq = new PriorityQueue<>();
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prevStation = new HashMap<>();
        Map<String, String> prevLine = new HashMap<>();

        for (String station : graph.keySet()) {
            dist.put(station, Double.MAX_VALUE);
        }
        dist.put(start, 0.0);
        pq.add(new Node(start, 0.0));

        // Dijkstra算法
        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current.station.equals(end))
                break;

            for (Connection conn : graph.get(current.station)) {
                double newDist = current.distance + conn.distance;
                if (newDist < dist.get(conn.target)) {
                    dist.put(conn.target, newDist);
                    prevStation.put(conn.target, current.station);
                    prevLine.put(conn.target, conn.line);
                    pq.add(new Node(conn.target, newDist));
                }
            }
        }

        // 回溯路径
        List<String> path = new ArrayList<>();
        String current = end;
        while (current != null) {
            path.add(0, current);
            current = prevStation.get(current);
        }

        return new PathResult(path, prevLine, dist.get(end));
    }

    /**
     * 辅助类，用于Dijkstra算法中的节点，包含站点名称和到起始点的距离。
     */
    static class Node implements Comparable<Node> {
        String station;
        double distance;

        /**
         * 构造一个新的Node对象。
         * 
         * @param station  站点名称
         * @param distance 到起始点的距离
         */
        public Node(String station, double distance) {
            this.station = station;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    /**
     * 路径结果封装类，包含最短路径信息、线路信息和总距离。
     */
    static class PathResult {
        List<String> path;
        Map<String, String> lineInfo;
        double totalDistance;

        /**
         * 构造一个新的PathResult对象。
         * 
         * @param path     最短路径列表
         * @param lineInfo 线路信息映射
         * @param distance 总距离
         */
        public PathResult(List<String> path, Map<String, String> lineInfo, double distance) {
            this.path = path;
            this.lineInfo = lineInfo;
            this.totalDistance = distance;
        }
    }

    /**
     * 格式化输出最短路径信息。
     * 
     * @param result 包含最短路径信息的PathResult对象
     */
    public String printPath(PathResult result) {
        if (result.path.isEmpty()) {
            return "未找到路径";
        }

        String currentLine = null;
        String startStation = result.path.get(0);

        for (int i = 1; i < result.path.size(); i++) {
            String station = result.path.get(i);
            String line = result.lineInfo.get(station);

            if (!line.equals(currentLine)) {
                if (currentLine != null) {
                    System.out.printf("在%s换乘%s号线 -> ", result.path.get(i - 1), line);
                }
                currentLine = line;
                System.out.printf("乘坐%s号线从%s到", line, startStation);
            }
            startStation = station;
        }
        return startStation;
    }

    /**
     * 根据距离计算票价。
     * 
     * @param distance 行程距离
     * @return 计算得到的票价
     */
    public double calculateFare(double distance) {
        double total;
        if (distance <= 4) {
            total = 2;
        } else if (distance > 4 && distance <= 12) {
            total = 2 + (distance - 4) * 0.25;
        } else if (distance > 12 && distance <= 24) {
            total = 4 + (distance - 12) * 1 / 6;
        } else if (distance > 24 && distance <= 40) {
            total = 6 + (distance - 24) * 0.125;
        } else if (distance > 40 && distance <= 50) {
            total = 8 + (distance - 40) * 0.1;
        } else if (distance > 50) {
            total = 10 + (distance - 50) * 0.05;
        } else {
            total = 0;
        }
        return total;
    }
}
