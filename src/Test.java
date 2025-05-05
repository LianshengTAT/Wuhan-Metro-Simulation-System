import java.io.IOException;
import java.util.Arrays;
import javax.swing.SwingUtilities;

/**
 * 测试类，用于测试SubwaySystem类的各项功能。
 */
public class Test {
    /**
     * 程序入口，执行各项地铁系统功能的测试。
     * 
     * @param args 命令行参数
     * @throws IOException 如果文件读取过程中出现错误
     */
    public static void main(String[] args) throws IOException {
        SubwaySystem subway = new SubwaySystem();
        subway.parseData("subway.txt");

        // 测试功能1
        System.out.println("换乘站列表：");
        subway.getTransferStations().forEach((k, v) -> System.out.println(k + " -> " + v));

        // 测试功能2
        System.out.println("\n邻近站点查询（华中科技大学站，距离1km）：");
        subway.getNearbyStations("华中科技大学站", 1.0).forEach(arr -> System.out.println(Arrays.toString(arr)));

        // 测试功能4
        System.out.println("\n最短路径查询（循礼门 -> 武汉火车站）：");
        SubwaySystem.PathResult path = subway.shortestPath("循礼门", "武汉火车站");
        String shotestPath = subway.printPath(path);
        System.out.println(shotestPath);

        // 测试功能6
        double fare = subway.calculateFare(path.totalDistance);
        System.out.printf("\n票价计算：普通票 %.1f元，武汉通 %.1f元\n",
                fare, fare * 0.9);

        SubwaySystem subwaySystem = new SubwaySystem();
        // 在时间调度线程中创建并显示图形界面
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SubwayGUI subwayGUI = new SubwayGUI(subwaySystem);
                subwayGUI.setVisible(true);
            }
        });
    }
}