import { Component, ElementRef, ViewChild, AfterViewInit, OnDestroy, OnInit, NgZone, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as THREE from 'three';
import { Subscription } from 'rxjs';
import { MetricsStreamService } from '../../../core/services/metrics-stream.service';

@Component({
  selector: 'app-three-scene',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './three-scene.component.html',
  styleUrls: ['./three-scene.component.scss']
})
export class ThreeSceneComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('canvasContainer', { static: true }) canvasContainer!: ElementRef<HTMLDivElement>;

  private renderer!: THREE.WebGLRenderer;
  private scene!: THREE.Scene;
  private camera!: THREE.PerspectiveCamera;
  private animationFrameId: number | null = null;
  private streamSubscription?: Subscription;

  // Particle System
  private particles!: THREE.Points;
  
  // Data Flow Variables
  private targetSpeed = 0.002;
  private currentSpeed = 0.002;

  constructor(
    private ngZone: NgZone,
    private metricsService: MetricsStreamService
  ) {}

  ngOnInit(): void {
    // Subscribe to SSE Metrics stream
    this.streamSubscription = this.metricsService.getTrafficStream().subscribe(trafficValue => {
      // Map traffic (1000 - 5000) to rotation speed (0.002 - 0.02)
      // High traffic = faster spinning particle field
      this.targetSpeed = this.mapRange(trafficValue, 1000, 5000, 0.002, 0.02);
    });
  }

  ngAfterViewInit(): void {
    this.initThreeJs();
    // Start animation loop outside Angular zone
    this.ngZone.runOutsideAngular(() => {
      this.animate();
    });
  }

  ngOnDestroy(): void {
    if (this.streamSubscription) {
      this.streamSubscription.unsubscribe();
    }
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
    }
    // Clean up WebGL resources
    if (this.renderer) {
      this.renderer.dispose();
    }
    if (this.particles) {
      this.particles.geometry.dispose();
      (this.particles.material as THREE.Material).dispose();
    }
  }

  private initThreeJs(): void {
    const container = this.canvasContainer.nativeElement;
    const width = container.clientWidth;
    const height = container.clientHeight;

    this.scene = new THREE.Scene();
    
    // Add subtle fog to blend particles into the background
    this.scene.fog = new THREE.FogExp2(0x080B14, 0.1);

    this.camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000);
    this.camera.position.z = 12;
    this.camera.position.y = 2;
    this.camera.lookAt(0, 0, 0);

    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    this.renderer.setSize(width, height);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    container.appendChild(this.renderer.domElement);

    this.createParticleField();
  }

  private createParticleField(): void {
    const particleCount = 4000;
    const geometry = new THREE.BufferGeometry();
    const positions = new Float32Array(particleCount * 3);
    const colors = new Float32Array(particleCount * 3);
    
    const colorPrimary = new THREE.Color(0xc084fc); // Purple
    const colorSecondary = new THREE.Color(0x3b82f6); // Blue

    for (let i = 0; i < particleCount; i++) {
      // Create a cylindrical distribution around the Y axis
      const radius = 2 + Math.random() * 8;
      const theta = Math.random() * Math.PI * 2;
      const y = (Math.random() - 0.5) * 15;

      positions[i * 3] = radius * Math.cos(theta); // x
      positions[i * 3 + 1] = y;                    // y
      positions[i * 3 + 2] = radius * Math.sin(theta); // z

      // Mix colors based on position
      const mixedColor = colorPrimary.clone().lerp(colorSecondary, Math.random());
      colors[i * 3] = mixedColor.r;
      colors[i * 3 + 1] = mixedColor.g;
      colors[i * 3 + 2] = mixedColor.b;
    }

    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));

    const material = new THREE.PointsMaterial({
      size: 0.15,
      vertexColors: true,
      transparent: true,
      opacity: 0.8,
      blending: THREE.AdditiveBlending
    });

    this.particles = new THREE.Points(geometry, material);
    
    // Tilt it slightly
    this.particles.rotation.x = 0.2;
    this.scene.add(this.particles);
  }

  private animate = (): void => {
    this.animationFrameId = requestAnimationFrame(this.animate);

    // Smooth interpolation (Lerp) towards the target speed
    this.currentSpeed += (this.targetSpeed - this.currentSpeed) * 0.05;
    
    if (this.particles) {
      // Rotate the entire field
      this.particles.rotation.y -= this.currentSpeed;
      
      // We can also slowly move the particles upwards to simulate flow
      const positions = this.particles.geometry.attributes['position'].array;
      for (let i = 0; i < positions.length; i += 3) {
        positions[i + 1] += this.currentSpeed * 2; // move Y up
        // wrap around if it goes too high
        if (positions[i + 1] > 7.5) {
          positions[i + 1] = -7.5;
        }
      }
      this.particles.geometry.attributes['position'].needsUpdate = true;
    }
    
    this.renderer.render(this.scene, this.camera);
  };

  @HostListener('window:resize')
  onWindowResize(): void {
    const container = this.canvasContainer.nativeElement;
    // ensure container has actual dimensions before resize
    if (!container || container.clientWidth === 0) return;
    
    const width = container.clientWidth;
    const height = container.clientHeight;

    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(width, height);
  }

  // Utility to map a range of numbers to another range
  private mapRange(value: number, inMin: number, inMax: number, outMin: number, outMax: number): number {
    // Clamp the value to the boundaries
    const clampedValue = Math.max(inMin, Math.min(value, inMax));
    return ((clampedValue - inMin) * (outMax - outMin)) / (inMax - inMin) + outMin;
  }
}
