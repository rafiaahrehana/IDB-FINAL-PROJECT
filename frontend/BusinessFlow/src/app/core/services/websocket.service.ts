import { Injectable, NgZone, signal } from '@angular/core';
import { Client, IMessage, IFrame } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Observable, Subject, filter, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface WebSocketMessage {
  type: string;
  payload: any;
  timestamp?: string;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client | null = null;
  private messageSubject = new Subject<WebSocketMessage>();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 10;
  private reconnectDelay = 3000;

  readonly connected = signal(false);
  readonly connecting = signal(false);

  constructor(private zone: NgZone) {}

  connect(token: string): void {
    if (this.client?.active) return;

    this.connecting.set(true);

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${environment.apiUrl.replace('/api', '')}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      debug: () => {},
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: (frame: IFrame) => {
        this.zone.run(() => {
          this.connected.set(true);
          this.connecting.set(false);
          this.reconnectAttempts = 0;
        });
      },
      onDisconnect: () => {
        this.zone.run(() => {
          this.connected.set(false);
        });
      },
      onStompError: (frame: IFrame) => {
        this.zone.run(() => {
          this.connected.set(false);
          this.connecting.set(false);
        });
        this.reconnectAttempts++;
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
          this.client?.deactivate();
        }
      },
    });

    this.client.activate();
  }

  disconnect(): void {
    if (this.client?.active) {
      this.client.deactivate();
    }
    this.connected.set(false);
    this.connecting.set(false);
  }

  subscribe<T = any>(destination: string): Observable<T> {
    return new Observable<T>(subscriber => {
      if (!this.client?.active) {
        subscriber.error('WebSocket not connected');
        return;
      }

      const subscription = this.client.subscribe(destination, (message: IMessage) => {
        this.zone.run(() => {
          try {
            const parsed = JSON.parse(message.body) as T;
            subscriber.next(parsed);
          } catch {
            subscriber.next(message.body as unknown as T);
          }
        });
      });

      return () => subscription.unsubscribe();
    });
  }

  subscribeToTopic<T = any>(topic: string): Observable<T> {
    return this.subscribe<T>(`/topic${topic}`);
  }

  subscribeToQueue<T = any>(queue: string): Observable<T> {
    return this.subscribe<T>(`/queue${queue}`);
  }

  sendMessage(destination: string, body: any): void {
    if (!this.client?.active) return;
    this.client.publish({
      destination,
      body: JSON.stringify(body),
    });
  }

  onMessage<T = any>(type: string): Observable<T> {
    return this.messageSubject.asObservable().pipe(
      filter(msg => msg.type === type),
      map(msg => msg.payload as T)
    );
  }
}
