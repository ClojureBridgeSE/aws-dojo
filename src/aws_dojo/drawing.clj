(ns aws-dojo.drawing
  (:require [hiccup.core :as hiccup]))

(defn- concatv [& xs]
  (->> (apply concat xs)
       (into [])))

(defn- rect [[x y] width height color]
  [:rect {:x x
          :y y
          :width width
          :height height
          :fill color}])

(defn- circle [[x y] radius color]
  [:circle {:cx x
            :cy y
            :r radius
            :fill color}])

(defn- ->svg-shape [command]
  (case (:command command)
    "drawRectangle"
    (let [{:keys [point width height color]} command]
      (rect point width height color))

    "drawCircle"
    (let [{:keys [center radius color]} command]
      (circle center radius color))))

(defn- create-svg [{:keys [width height color]}]
                  shapes
  (let [svg-shapes (mapv ->svg-shape shapes)]
    (hiccup/html (concatv
                   [:svg {:height height
                          :width width}
                    (rect [0 0] width height color)]
                   svg-shapes))))

(defn- create-drawing? [{:keys [command]}]
  (= command "createDrawing"))

(defn complete? [commands]
  (if-let [[create-cmd] (filter create-drawing? commands)]
    (let [shapes (remove create-drawing? commands)]
      (= (:shapes create-cmd) (count shapes)))
    false))

(defn render [commands]
  (let [[frame] (filter create-drawing? commands)
        shapes (->> commands
                    (remove create-drawing?)
                    (sort-by :z))]
    (hiccup/html [:html
                  [:head [:title (:title frame)]]
                  [:body (create-svg frame shapes)]])))

;;Extras!
(def sample-commands [{:command "createDrawing", :title "Hello, world!", :name "hello-world", :color "#808080", :width 640, :height 480, :shapes 2}
                      {:command "drawRectangle", :color "white", :point [20 40], :width 600, :height 400}
                      {:command "drawCircle", :color "red", :center [320 240], :radius 150, :z 1}])

(defn test []
  (render sample-commands))

(defn create-html []
  (spit "ponies.html" (test)))
