import React, { useState, useEffect } from "react";
import { useTheme } from "@material-ui/styles";
import {
  Grid,
} from "@material-ui/core";
import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  YAxis,
  XAxis,
} from "recharts";
import { useSubscription } from "react-stomp-hooks";

// components
import PageTitle from "../../components/PageTitle/PageTitle";
import Widget from "../../components/Widget";
import { Typography } from "../../components/Wrappers";
import Dot from "../../components/Sidebar/components/Dot";
import mock from "../dashboard/mock";
import Table from "./components/Table";
import DNSDetections from "./components/DNSDetections/DNSDetections";

// styles
import useStyles from "./styles";

const mainChartData = getMainChartData();

export default function Detections(props) {
  var classes = useStyles();
  var theme = useTheme();
  const [dnsData, setDnsData] = useState()


  useSubscription("/topic/dns-data", (dns) => {
    console.log(setDnsData(JSON.parse(dns.body)));
  });


  return (
    <>
      <PageTitle title="Detections" />

      <Grid container spacing={4}>
        <Grid item xs={12}>
            <Widget
              bodyClass={classes.mainChartBody}
              header={
                <div className={classes.mainChartHeader}>
                  <Typography
                    variant="h5"
                    color="text"
                    colorBrightness="secondary"
                  >
                    DNS Detections
                  </Typography>
                  <div className={classes.mainChartHeaderLabels}>
                    <div className={classes.mainChartHeaderLabel}>
                      <Dot color="warning" />
                      <Typography className={classes.mainChartLegentElement}>
                        Tablet
                      </Typography>
                    </div>
                    <div className={classes.mainChartHeaderLabel}>
                      <Dot color="primary" />
                      <Typography className={classes.mainChartLegentElement}>
                        Mobile
                      </Typography>
                    </div>
                  </div>
                </div>
              }
            >
              <ResponsiveContainer width="100%" minWidth={500} height={350}>
                <ComposedChart
                  margin={{ top: 0, right: -15, left: -15, bottom: 0 }}
                  data={mainChartData}
                >
                  <YAxis
                    //ticks={[0, 2500, 5000, 7500]}
                    tick={{ fill: theme.palette.text.hint + "80", fontSize: 14 }}
                    stroke={theme.palette.text.hint + "80"}
                    tickLine={false}
                  />
                  <XAxis
                    //tickFormatter={i => i + 1}
                    tick={{ fill: theme.palette.text.hint + "80", fontSize: 14 }}
                    stroke={theme.palette.text.hint + "80"}
                    tickLine={false}
                  />
                  <Line
                    type="natural"
                    dataKey="mobile"
                    stroke={theme.palette.primary.main}
                    strokeWidth={2}
                    dot={false}
                    activeDot={false}
                  />
                  <Line
                    type="linear"
                    dataKey="tablet"
                    stroke={theme.palette.warning.main}
                    strokeWidth={2}
                    dot={{
                      stroke: theme.palette.warning.dark,
                      strokeWidth: 2,
                      fill: theme.palette.warning.main,
                    }}
                  />
                </ComposedChart>
              </ResponsiveContainer>
            </Widget>
          </Grid>
          <DNSDetections dnsData={dnsData} />
        </Grid>
    </>
  );
}

function getRandomData(length, min, max, multiplier = 10, maxDiff = 10) {
  var array = new Array(length).fill();
  let lastValue;

  return array.map((item, index) => {
    let randomValue = Math.floor(Math.random() * multiplier + 1);

    while (
      randomValue <= min ||
      randomValue >= max ||
      (lastValue && randomValue - lastValue > maxDiff)
    ) {
      randomValue = Math.floor(Math.random() * multiplier + 1);
    }

    lastValue = randomValue;

    return { value: randomValue };
  });
}

function getMainChartData() {
  var resultArray = [];
  var tablet = getRandomData(31, 3500, 6500, 7500, 1000);
  var desktop = getRandomData(31, 1500, 7500, 7500, 1500);
  var mobile = getRandomData(31, 1500, 7500, 7500, 1500);

  for (let i = 0; i < tablet.length; i++) {
    resultArray.push({
      tablet: tablet[i].value,
      desktop: desktop[i].value,
      mobile: mobile[i].value,
    });
  }

  return resultArray;
}
