import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ThreeSceneComponent } from '../../../../shared/components/three-scene/three-scene.component';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [RouterLink, ThreeSceneComponent],
  templateUrl: './hero.html',
  styleUrls: ['./hero.scss']
})
export class HeroComponent {
}
