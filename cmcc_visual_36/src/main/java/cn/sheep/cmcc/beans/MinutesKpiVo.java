package cmcc_visual_36.src.main.java.cn.sheep.cmcc.beans;

/**
 * 每分钟充值成功数量和金额
 */
public class MinutesKpiVo {
    private String money;
    private String counts;

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getCounts() {
        return counts;
    }

    public void setCounts(String counts) {
        this.counts = counts;
    }

    public MinutesKpiVo(String money, String counts) {
        this.money = money;
        this.counts = counts;
    }

    public MinutesKpiVo() {
    }
}
