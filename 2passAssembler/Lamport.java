/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Lamport {

    class Message {

        int timestamp;
        int from;
        int to;

        public Message(int timestamp, int from, int to) {
            this.timestamp = timestamp;
            this.from = from;
            this.to = to;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

    }

    class MessageMonitor {

        Queue< Message> message_queue;

        public MessageMonitor() {
            message_queue = new LinkedList();
        }

        Message getMessage(int to) {
            System.out.println(message_queue.size());
            for (Message tmp : message_queue) {
                System.out.println(tmp.getTo());
                if (tmp.getTo() == to) {
                    System.out.println("Recieved by" + to + "  from " + tmp.from);
                    message_queue.remove(tmp);
                    return tmp;
                }
            }
            return null;
        }

        Queue< Message> getInstanse() {
            return message_queue;
        }

        void sendMessage(Message msg) {
            System.out.println("Added");
            message_queue.add(msg);

        }
    }

    class Process extends Thread {

        int noevent;
        int timestamp;
        int processno;
        int incomingtimestamp;
        int internalcounter;
        MessageMonitor mm;
        ArrayList<event> einfo;
        Queue< Message> lock;
        ArrayList<Integer> ans;

        public Process(int a, ArrayList<event> b, int c, MessageMonitor d, Queue< Message> l) {
            this.noevent = a;
            this.einfo = b;
            this.processno = c;
            timestamp = 0;
            this.mm = d;
            internalcounter = 1;
            this.lock = l;
            ans = new ArrayList<>();
        }

        @Override
        public void run() {
            while (internalcounter <= noevent) {

                boolean stop = false;
                for (int i = 0; i < einfo.size(); i++) {
                    if (einfo.get(i).pend == processno && einfo.get(i).end == internalcounter) {
                        stop = true;
                        break;
                    }
                }
                if (stop) {
                    Message tmp;

                    synchronized (lock) {
                        while (true) {
                            tmp = mm.getMessage(processno);
                            if (tmp == null) {
                                try {
                                    System.out.println("Thread no " + processno + "will wait");
                                    lock.wait();
                                    System.out.println("Thread no " + processno + "woke");
                                } catch (InterruptedException ex) {
                                    System.out.println(ex.getMessage());
                                }
                            } else {
                                break;
                            }
                        }
                    }

                    if (tmp.getTimestamp() > timestamp + 1) {
                        timestamp = tmp.getTimestamp();
                    } else {
                        timestamp += 1;
                    }

                } else {
                    timestamp += 1;
                }
                System.out.println("Process " + processno + "  " + internalcounter + "  ->" + timestamp);
                for (int i = 0; i < einfo.size(); i++) {
                    if (einfo.get(i).pstart == processno && einfo.get(i).start == internalcounter) {

                        System.out.println("Sending message to Thread no to " + einfo.get(i).pend + " by " + einfo.get(i).pstart);
                        synchronized (lock) {
                            mm.sendMessage(new Message(timestamp + 1, processno, einfo.get(i).pend));
                            lock.notifyAll();
                        }
                    }

                }
                internalcounter++;
                ans.add(timestamp);
            }
            printTimestamp();

        }

        void printTimestamp() {
            System.out.println("Thread no : " + processno + " Completed" + " [ TimeStamp " + Arrays.toString(ans.toArray()) + " ]");
        }

    }

    class event {

        int pstart;
        int pend;
        int start;
        int end;

        public event(int pstart, int start, int pend, int end) {
            this.pstart = pstart;
            this.pend = pend;
            this.start = start;
            this.end = end;
        }
    }
    int nodes;
    int eevent;
    ArrayList<Integer> size;
    ArrayList<event> einfo;
    int[][] store;

    public static void main(String[] args) {

        new Lamport().runner();

    }

    void runner() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number nodes : ");
        nodes = sc.nextInt();
        size = new ArrayList<>();
        store = new int[10][10];
        System.out.println("Enter each node size : ");
        for (int i = 0; i < nodes; i++) {
            size.add(sc.nextInt());
        }

        System.out.println("Enter number of external event : ");
        eevent = sc.nextInt();
        einfo = new ArrayList<>();
        for (int i = 0; i < eevent; i++) {
            System.out.print("P(a) event || P(b) event : ");
            einfo.add(new event(sc.nextInt(), sc.nextInt(), sc.nextInt(), sc.nextInt()));
        }
        processthread();

    }

    void processthread() {
        MessageMonitor mm = new MessageMonitor();
        Queue< Message> ins = mm.getInstanse();
        for (int x = 1; x <= nodes; x++) {
            Process tmp = new Process(size.get(x - 1), einfo, x, mm, ins);
            Thread a = new Thread(tmp);
            a.start();
        }
    }

    void process() {
        for (int x = 0; x < nodes; x++) {
            for (int y = 0; y < size.get(x); y++) {
                if (y == 0) {
                    store[x][y] = 1;
                } else {
                    store[x][y] = store[x][y - 1] + 1;
                }
            }
        }
        System.out.println("");
        //printall();
        solve();
        printall();

    }

    void printall() {
        for (int x = 0; x < nodes; x++) {
            System.out.print("Node " + x + " : ");

            for (int y = 0; y < size.get(x); y++) {
                System.out.print(store[x][y] + " ");
            }
            System.out.println("");
        }

    }

    void solve() {
        for (int count = 0; count < einfo.size(); count++) {
            int endbefore = einfo.get(count).end - 2;
            if (einfo.get(count).end - 2 < 0) {
                endbefore = 0;
            }
            //System.out.println(store[einfo.get(count).pstart - 1][einfo.get(count).start - 1] + 1 + " " + (store[einfo.get(count).pend - 1][endbefore] + 1));
            if (store[einfo.get(count).pstart - 1][einfo.get(count).start - 1] + 1 >= store[einfo.get(count).pend - 1][endbefore] + 1) {
                store[einfo.get(count).pend - 1][einfo.get(count).end - 1] = store[einfo.get(count).pstart - 1][einfo.get(count).start - 1] + 1;
                for (int i = einfo.get(count).end; i < size.get(einfo.get(count).pend - 1); i++) {
                    store[einfo.get(count).pend - 1][i] = store[einfo.get(count).pend - 1][i - 1] + 1;
                }

            }

        }

    }

}