package top.leejay.design.delegate.example;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * boss 指挥 leader
 */
public class Boss {
    private Leader leader;

    public Boss(Leader leader) {
        this.leader = leader;
    }

    public void command(String command) {
        leader.doing(command);
    }

    public static void main(String[] args) {
        Boss boss = new Boss(new Leader());
        String command = "加密";
        System.out.println("Boss 下命令: " + command);
        boss.command(command);
    }
}
