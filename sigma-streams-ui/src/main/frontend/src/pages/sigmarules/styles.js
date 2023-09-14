import { makeStyles } from "@material-ui/styles";

export default makeStyles(theme => ({
    ruleViewer: {
        width: "100%",
    },
    button: {
        backgroundColor: '#152952',
        color: '#fff',
        width: 200, 
        height: 40,
        "&:hover": {
            backgroundColor: "#67728C",
          }, 
        "&:disabled": {
            backgroundColor: "#67728C",
          }, 
    },
    notificationItem: {
        marginTop: theme.spacing(2),
      },
      notificationCloseButton: {
        position: "absolute",
        right: theme.spacing(2),
      },
      toastsContainer: {
        width: 400,
        marginTop: theme.spacing(6),
        right: 0,
      },
      progress: {
        visibility: "hidden",
      },
      notification: {
        display: "flex",
        alignItems: "center",
        background: "transparent",
        boxShadow: "none",
        overflow: "visible",
      },
      notificationComponent: {
        paddingRight: theme.spacing(4),
      },
}));