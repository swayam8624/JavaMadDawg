# UDP Networking Protocol

The engine provides a basic UDP socket client-server replication layer.

---

## 1. Packet Structure
Each network message is sent as a `Packet` containing a single byte type identifier followed by custom payloads:
* `0x01` (LOGIN): Handshake connecting the client.
* `0x02` (DISCONNECT): Clean disconnection notification.
* `0x03` (MOVE): Replicates player positions coordinates.

---

## 2. Server authoritativeness
The `GameServer` manages client coordinates. Incoming position packets update positions, and the server broadcasts coordinates to all other clients, allowing replication.
To handle packet drops, a production engine should build packet sequencing and state interpolation.
