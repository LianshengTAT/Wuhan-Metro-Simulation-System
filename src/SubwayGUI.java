import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SubwayGUI extends JFrame {
    private SubwaySystem SubwaySystem;
    private JTextArea textArea;

    public SubwayGUI(SubwaySystem subwaySystem) {
        this.SubwaySystem = subwaySystem;
        createUI();
    }

    public void createUI() {
        setTitle("Wuhan Metro Simulation System");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        textArea = new JTextArea("Welcome to Wuhan Metro Simulation System.", 10, 30);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JButton btnTransferStation = new JButton("显示中转站");
        JButton btnNearbyStation = new JButton("查询邻近站点");
        JButton btnPathResult = new JButton("路线规划");

        btnTransferStation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SubwaySystem.getTransferStations().forEach((k, v) -> System.out.println(k + " -> " + v));
            }
        });

        btnNearbyStation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String stationName = JOptionPane.showInputDialog("请输入站点名称：");
                String distance = JOptionPane.showInputDialog("请输入距离：");
                try {
                    int Distance = Integer.parseInt(distance);
                    SubwaySystem.getNearbyStations(stationName, Distance)
                            .forEach(arr -> System.out.println(Arrays.toString(arr)));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(SubwayGUI.this, e, "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnPathResult.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String startAddition = JOptionPane.showInputDialog("请输入出发站点：");
                String endAddition = JOptionPane.showInputDialog("请输入目的地：");
                SubwaySystem.PathResult path = SubwaySystem.shortestPath(startAddition, endAddition);
                String shotestPath = SubwaySystem.printPath(path);
                textArea.setText(shotestPath);
            }
        });

        panel.add(btnTransferStation);
        panel.add(btnNearbyStation);
        panel.add(btnPathResult);
        add(panel, BorderLayout.SOUTH);

    }
}