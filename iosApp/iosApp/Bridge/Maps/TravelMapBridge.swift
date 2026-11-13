import ComposeApp
import GoogleMaps
import UIKit

final class GoogleTravelMapFactory: NSObject, TravelMapHostFactory {
    func attach(
        container: UIView,
        onMarkerClick: @escaping (String) -> Void,
        onUnavailable: @escaping () -> Void
    ) -> any TravelMapHost {
        GoogleTravelMap(container: container, onMarkerClick: onMarkerClick, onUnavailable: onUnavailable)
    }
}

private final class GoogleTravelMap: NSObject, TravelMapHost, GMSMapViewDelegate {
    private let map: GMSMapView
    private let surface = TravelMapSurface()
    private let onMarkerClick: (String) -> Void
    private var tileTimeout: DispatchWorkItem?
    private var previousViewport: Int32?
    private var previousFocus: String?
    private var previouslyEmpty = true
    private var pendingCamera: (() -> Void)?

    init(container: UIView, onMarkerClick: @escaping (String) -> Void, onUnavailable: @escaping () -> Void) {
        map = GMSMapView(options: GMSMapViewOptions())
        self.onMarkerClick = onMarkerClick
        super.init()
        surface.translatesAutoresizingMaskIntoConstraints = false
        container.addSubview(surface)
        map.frame = surface.bounds
        map.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        surface.addSubview(map)
        NSLayoutConstraint.activate([
            surface.leadingAnchor.constraint(equalTo: container.leadingAnchor),
            surface.trailingAnchor.constraint(equalTo: container.trailingAnchor),
            surface.topAnchor.constraint(equalTo: container.topAnchor),
            surface.bottomAnchor.constraint(equalTo: container.bottomAnchor)
        ])
        surface.onLayout = { [weak self] in self?.applyPendingCamera() }
        map.delegate = self
        let timeout = DispatchWorkItem(block: onUnavailable)
        tileTimeout = timeout
        DispatchQueue.main.asyncAfter(deadline: .now() + 15, execute: timeout)
    }

    func render(scene: TravelMapScene, viewportRequest: Int32, focusedVisitId: String?) {
        map.clear()
        for line in scene.lines {
            let path = GMSMutablePath()
            line.points.forEach { path.add($0.coordinate) }
            let polyline = GMSPolyline(path: path)
            polyline.geodesic = true
            polyline.strokeColor = .travelRoute
            polyline.strokeWidth = 3
            polyline.map = map
        }
        for pin in scene.markers {
            let marker = GMSMarker(position: pin.coordinates.coordinate)
            marker.icon = makeIcon(label: pin.label, colorArgb: pin.colorArgb)
            marker.groundAnchor = CGPoint(x: 0.5, y: 0.5)
            marker.zIndex = pin.selected ? 1 : 0
            marker.userData = pin.id
            marker.map = map
        }

        let isEmpty = scene.markers.isEmpty
        guard previousViewport != viewportRequest || previousFocus != focusedVisitId || previouslyEmpty != isEmpty else { return }
        previousViewport = viewportRequest
        previousFocus = focusedVisitId
        previouslyEmpty = isEmpty
        pendingCamera = { [weak self] in self?.positionCamera(for: scene, focusedVisitId: focusedVisitId) }
        applyPendingCamera()
    }

    private func applyPendingCamera() {
        guard !map.bounds.isEmpty, let update = pendingCamera else { return }
        pendingCamera = nil
        update()
    }

    private func positionCamera(for scene: TravelMapScene, focusedVisitId: String?) {
        if let focused = scene.markers.first(where: { $0.id == focusedVisitId }) {
            map.camera = GMSCameraPosition(target: focused.coordinates.coordinate, zoom: 15)
        } else if let first = scene.markers.first {
            let origin = first.coordinates.coordinate
            let otherPoints = scene.markers.dropFirst().filter {
                $0.coordinates.latitude != origin.latitude || $0.coordinates.longitude != origin.longitude
            }
            if otherPoints.isEmpty {
                map.camera = GMSCameraPosition(target: origin, zoom: 14)
            } else {
                let bounds = otherPoints.reduce(GMSCoordinateBounds(coordinate: origin, coordinate: origin)) {
                    $0.includingCoordinate($1.coordinates.coordinate)
                }
                map.moveCamera(GMSCameraUpdate.fit(bounds, withPadding: 28))
            }
        } else {
            let center = scene.cityCenter?.coordinate ?? CLLocationCoordinate2D(latitude: 0, longitude: 0)
            map.camera = GMSCameraPosition(target: center, zoom: scene.cityCenter == nil ? 1 : 11)
        }
    }

    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        if let id = marker.userData as? String { onMarkerClick(id) }
        return true
    }

    func mapViewSnapshotReady(_ mapView: GMSMapView) {
        tileTimeout?.cancel()
        tileTimeout = nil
    }

    func dispose() {
        tileTimeout?.cancel()
        map.delegate = nil
        pendingCamera = nil
        surface.onLayout = nil
        surface.removeFromSuperview()
    }

    private func makeIcon(label: String?, colorArgb: Int32) -> UIImage {
        let size = CGSize(width: 34, height: 34)
        return UIGraphicsImageRenderer(size: size).image { _ in
            UIColor.white.setFill()
            UIBezierPath(ovalIn: CGRect(origin: .zero, size: size)).fill()
            let color = UInt32(bitPattern: colorArgb)
            UIColor(
                red: CGFloat((color >> 16) & 0xff) / 255,
                green: CGFloat((color >> 8) & 0xff) / 255,
                blue: CGFloat(color & 0xff) / 255,
                alpha: 1
            ).setFill()
            UIBezierPath(ovalIn: CGRect(x: 2, y: 2, width: 30, height: 30)).fill()
            guard let label else { return }
            let attributes: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: 14),
                .foregroundColor: UIColor.white
            ]
            let text = NSAttributedString(string: label, attributes: attributes)
            let bounds = text.size()
            text.draw(at: CGPoint(x: (size.width - bounds.width) / 2, y: (size.height - bounds.height) / 2))
        }
    }
}

private final class TravelMapSurface: UIView {
    var onLayout: (() -> Void)?

    override func layoutSubviews() {
        super.layoutSubviews()
        onLayout?()
    }
}

private extension UIColor {
    static let travelRoute = UIColor(red: 0, green: 0.49, blue: 0.41, alpha: 1)
}

private extension TravelCoordinates {
    var coordinate: CLLocationCoordinate2D { CLLocationCoordinate2D(latitude: latitude, longitude: longitude) }
}
