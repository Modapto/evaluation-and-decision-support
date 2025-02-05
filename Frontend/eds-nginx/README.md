# Evaluation and Decision support Toolkit (EDS)

Run with Docker and Docker Compose.

This component assists on the production process evaluation through visualisation dashboards, offering insights into the efficiency and effectiveness of production processes. It handles data and events from internal Smart Services, providing intuitive visualizations and multi-purpose dashboards for decision- making and monitoring of the registered MODAPTO modules.

Based on the Advanced Visualisation Toolkit (AVT) developed by Aegis IT Research:

* [AEGIS](https://aegisresearch.eu/)
* [AVT](https://avt.aegisresearch.eu/?_gl=1%2A1vwo23l%2A_ga%2AMTg2MTY1OTMzOS4xNzM4NjczNjIz%2A_ga_F3BQXKXREW%2AMTczODY3MzYyMy4xLjAuMTczODY3MzYyMy4wLjAuMA..)

---
# Setup the EDS 

## tl;dr

```sh
docker-compose up
```
---

## Requirements

### Host setup

* Docker Engine version **18.06.0** or newer
* Docker Compose version **1.28.0** or newer
* 2 GB of RAM

By default, it exposes the following port:

* 4200

## Usage

Give it about a minute to initialize, then access the web UI by opening <http://localhost:4200/fvt> in a web
browser.


> [!NOTE]
> Make sure you add */fvt/* on your url 

---

## Deployed EDS on ATC cloud

Since the latest version is deployed on the cloud you can access <https://fvt.modapto.atc.gr/fvt/> in a web browser.
  
---

## License

This project has received funding from the European Union's Horizon 2022 research and innovation programm, under Grant Agreement 101091996.

For more details about the licence, see the [LICENSE](LICENSE) file.

## Contributors

- Stella Markopoulou (<s.markopoulou@aegisresearch.eu>)
