package ru.itm.initbc.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Interface {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "mac")
    private String mac;
    @Column(name = "ip")
    private String ip;
    @Column(name = "ip_vpn")
    private String ip_vpn;
    private int priority;
    private boolean active;

    public Interface(String name, String mac, String ip, String ip_vpn, int priority, boolean active) {
        this.name = name;
        this.mac = mac;
        this.ip = ip;
        this.ip_vpn = ip_vpn;
        this.priority = priority;
        this.active = active;
    }
}
