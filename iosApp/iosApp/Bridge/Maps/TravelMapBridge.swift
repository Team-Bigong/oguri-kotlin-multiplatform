import CoreText
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
    private var fittedCoordinates = false
    private var pendingCamera: (() -> Void)?
    private var routeLines: [(GMSPolyline, Double)] = []
    private var pinFonts: [String: CGFont] = [:]

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

    func render(
        scene: TravelMapScene,
        viewportRequest: Int32,
        focusedVisitId: String?,
        pinStyle: TravelMapPinStyle,
        distanceLabels: [String],
        distanceStyle: TravelMapDistanceStyle,
        darkTheme: Bool
    ) {
        map.overrideUserInterfaceStyle = darkTheme ? .dark : .light
        map.clear()
        routeLines.removeAll()
        for (line, label) in zip(scene.lines, distanceLabels) {
            let start = line.start.coordinate
            let end = line.end.coordinate
            let path = GMSMutablePath()
            path.add(start)
            path.add(end)
            let polyline = GMSPolyline(path: path)
            polyline.geodesic = true
            polyline.strokeColor = UIColor(argb: distanceStyle.connectionArgb)
            polyline.strokeWidth = 2
            polyline.map = map
            routeLines.append((polyline, GMSGeometryDistance(start, end)))

            let marker = GMSMarker(position: line.midpoint.coordinate)
            marker.icon = makeDistanceIcon(label: label, style: distanceStyle)
            marker.groundAnchor = CGPoint(x: 0.5, y: 0.5)
            marker.zIndex = -1
            marker.isTappable = false
            marker.map = map
        }
        updateLinePattern()
        for pin in scene.markers {
            let marker = GMSMarker(position: pin.coordinates.coordinate)
            marker.title = pin.title ?? pin.label
            marker.icon = makeIcon(
                label: pin.label,
                colorArgb: pin.colorArgb,
                labelColorArgb: pin.labelColorArgb,
                highlightColorArgb: pin.highlightColorArgb,
                selected: pin.selected,
                style: pinStyle
            )
            marker.groundAnchor = CGPoint(x: 0.5, y: 0.5)
            marker.zIndex = pin.selected ? 1 : 0
            marker.userData = pin.id
            marker.map = map
        }

        let hasCoordinates = !scene.viewportCoordinates.isEmpty
        guard previousViewport != viewportRequest || (!fittedCoordinates && hasCoordinates) else { return }
        previousViewport = viewportRequest
        fittedCoordinates = hasCoordinates
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
        } else if let first = scene.viewportCoordinates.first {
            let origin = first.coordinate
            let otherPoints = scene.viewportCoordinates.dropFirst().filter {
                $0.latitude != origin.latitude || $0.longitude != origin.longitude
            }
            if otherPoints.isEmpty {
                map.camera = GMSCameraPosition(target: origin, zoom: 14)
            } else {
                let bounds = otherPoints.reduce(GMSCoordinateBounds(coordinate: origin, coordinate: origin)) {
                    $0.includingCoordinate($1.coordinate)
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

    func mapView(_ mapView: GMSMapView, idleAt position: GMSCameraPosition) {
        updateLinePattern()
    }

    private func updateLinePattern() {
        let center = CGPoint(x: map.bounds.midX, y: map.bounds.midY)
        let meters = GMSGeometryDistance(map.projection.coordinate(for: center), map.projection.coordinate(for: CGPoint(x: center.x + 1, y: center.y)))
        guard meters.isFinite, meters > 0 else { return }
        let totalLength = routeLines.reduce(0) { $0 + $1.1 }
        let spacing = max(meters, totalLength / 32_000)
        for (polyline, _) in routeLines {
            guard let path = polyline.path else { continue }
            polyline.spans = GMSStyleSpans(path, [.solidColor(polyline.strokeColor), .solidColor(.clear)], [NSNumber(value: spacing * 2), NSNumber(value: spacing * 6)], .geodesic)
        }
    }

    func mapViewSnapshotReady(_ mapView: GMSMapView) {
        tileTimeout?.cancel()
        tileTimeout = nil
    }

    func dispose() {
        tileTimeout?.cancel()
        map.delegate = nil
        pendingCamera = nil
        routeLines.removeAll()
        pinFonts.removeAll()
        surface.onLayout = nil
        surface.removeFromSuperview()
    }

    private func pinFont(uri: String, size: CGFloat) -> UIFont {
        if pinFonts[uri] == nil, let url = URL(string: uri), let provider = CGDataProvider(url: url as CFURL), let font = CGFont(provider) {
            CTFontManagerRegisterGraphicsFont(font, nil)
            pinFonts[uri] = font
        }
        if let name = pinFonts[uri]?.postScriptName, let font = UIFont(name: name as String, size: size) { return font }
        return UIFont.systemFont(ofSize: size)
    }

    private func makeDistanceIcon(label: String, style: TravelMapDistanceStyle) -> UIImage {
        let font = pinFont(uri: style.fontUri, size: CGFloat(style.fontSize))
        let text = NSAttributedString(string: label, attributes: [
            .font: font,
            .foregroundColor: UIColor(argb: style.textArgb)
        ])
        let outlineWidth = CGFloat(style.outlineWidth)
        let outline = NSAttributedString(string: label, attributes: [
            .font: font,
            .strokeColor: UIColor(argb: style.outlineArgb),
            .strokeWidth: 200 * outlineWidth / font.pointSize
        ])
        let bounds = text.size()
        let size = CGSize(width: ceil(bounds.width + 2 * outlineWidth), height: ceil(bounds.height + 2 * outlineWidth))
        return UIGraphicsImageRenderer(size: size).image { _ in
            let origin = CGPoint(x: outlineWidth, y: outlineWidth)
            outline.draw(at: origin)
            text.draw(at: origin)
        }
    }

    private func makeIcon(label: String?, colorArgb: Int32, labelColorArgb: Int32, highlightColorArgb: Int32, selected: Bool, style: TravelMapPinStyle) -> UIImage {
        let side: CGFloat = selected ? 44 : 26
        let size = CGSize(width: side, height: side)
        return UIGraphicsImageRenderer(size: size).image { _ in
            let color = UIColor(argb: colorArgb)
            if selected {
                UIColor(argb: highlightColorArgb).setFill()
                UIBezierPath(ovalIn: CGRect(origin: .zero, size: size)).fill()
            }
            let diameter: CGFloat = selected ? 32 : 26
            let frame = CGRect(x: (side - diameter) / 2, y: (side - diameter) / 2, width: diameter, height: diameter)
            UIColor(argb: style.outlineArgb).setFill()
            UIBezierPath(ovalIn: frame).fill()
            color.setFill()
            UIBezierPath(ovalIn: frame.insetBy(dx: 2, dy: 2)).fill()
            guard let label else { return }
            let text = NSAttributedString(string: label, attributes: [
                .font: pinFont(uri: selected ? style.selectedLabelFontUri : style.labelFontUri, size: CGFloat(selected ? style.selectedLabelSize : style.labelSize)),
                .foregroundColor: UIColor(argb: labelColorArgb)
            ])
            let bounds = text.size()
            text.draw(at: CGPoint(x: (side - bounds.width) / 2, y: (side - bounds.height) / 2))
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
    convenience init(argb: Int32) {
        let color = UInt32(bitPattern: argb)
        self.init(red: CGFloat((color >> 16) & 0xff) / 255,
                  green: CGFloat((color >> 8) & 0xff) / 255,
                  blue: CGFloat(color & 0xff) / 255,
                  alpha: CGFloat((color >> 24) & 0xff) / 255)
    }
}

private extension TravelCoordinates {
    var coordinate: CLLocationCoordinate2D { CLLocationCoordinate2D(latitude: latitude, longitude: longitude) }
}
