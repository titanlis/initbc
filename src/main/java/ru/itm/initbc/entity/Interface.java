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

    @Column(name = "name", unique = true)
    private String name;
    @Column(name = "mac")
    private String mac;
    @Column(name = "ip", unique = true)
    private String ip;
    private int priority;
    private boolean active;

    public Interface(String name, String mac, String ip, int priority, boolean active) {
        this.name = name;
        this.mac = mac;
        this.ip = ip;
        this.priority = priority;
        this.active = active;
    }
}
