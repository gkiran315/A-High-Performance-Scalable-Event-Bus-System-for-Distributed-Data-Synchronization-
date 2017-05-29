/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


public class EventBusConfig {
    
    private static final EventBusConfig config = new EventBusConfig();
            
    private EventBusConfig(){}        
            
    
    private String ip_addr = "127.0.0.1";
    private int port_num = 8080;

    public String getIp_addr() {
        return ip_addr;
    }

    public void setIp_addr(String ip_addr) {
        this.ip_addr = ip_addr;
    }

    public int getPort_num() {
        return port_num;
    }

    public void setPort_num(int port_num) {
        this.port_num = port_num;
    }
    
    public static EventBusConfig getInstance(){
        return config;
    }
    
}
