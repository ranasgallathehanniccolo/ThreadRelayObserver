/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package threadrelay;

/**
 *
 * @author Windows
 */
public interface CorridoreObserver {
    void onAggiornamento(int id, int count);
    void onFine(int id);
}
