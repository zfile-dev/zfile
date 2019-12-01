// package im.zhaojun.onedrive.config;
//
// import org.nuxeo.onedrive.client.*;
// import org.springframework.context.annotation.Configuration;
//
// @Configuration
// public class OneDriveConfig {
//
//
//     public void a () {
//         OneDriveAPI api = new OneDriveBasicAPI("YOUR_ACCESS_TOKEN");
//
//         OneDriveFolder folder = new OneDriveFolder(api, "FOLDER_ID");
//         OneDriveFile file = new OneDriveFile(api, "FILE_ID");
//     }
//
//     public static void main(String[] args) throws OneDriveAPIException {
//         OneDriveBasicAPI api = new OneDriveBasicAPI("EwAgA61DBAAUcSSzoTJJsy+XrnQXgAKO5cj4yc8AAQ0ZknDUY8YnwB9aJv7vA9YjiRAVMnKc+rG11fSXLZRAA8Q/CgJaz+OkRN60vaLDfp6KxbmVlob6kxeD/peOUI2eHtk0055Q2+n057tlyVAvGIFl9dvqkItoAthjmybcSkKBZS5h1meWxQ5IOvzSVrdgCKL0NOtTxfh33ZUDsYjvSid6NOX4Bs+pRjvZhQkvqEfGt8KlOL+JoIowmv2I+u09iDmS60BMwSoeK2K3CCLIXxLaiiPYUMsrNk65j4PWEBwBEfyHb6j3lrM/YvwFLq7Y8KJVjrXjFENC7ruja6Ko/cfTMX90yLkUEckpsZ30E6RJHWEHt7jXtNwndDZVknYDZgAACL5pnk17FJfb8AGGxJL1Y0CnAzgkTM2gw+WkFRRDDNzujuW1LQofwZ119HdeANhPrBZ14x32VaPGL1l0RvtR9LCeAN+EogcV5xhVpmCExitaXQB6OkZ6BnXaxLj5TNvFRNeZq0ZfJ3T08clLA1vXHkZhNKgiFDI8xUbahy4r6QpzgoF+0+dz+MA1NzQCQCsRGieS63OD1BKrzRsNxzls5Z9rKzBT6CpWpiaiOg4mmW0yeino/L9zz9Gf5kAJr813bpNr+rH/E8MPd0pZf+6hv37FaVCM7RN1V7CkkCDnRAxwxEK8pDgZhRjZOw7gKutPOiOoTO9ptjh2Jcrds714HitX2HI3RsRY+yyAOcb8XI27m4daSEGCJCuu/TJwXTE4ul54MWsi8MrcDlZN9DOjckiJIqVI8IbvhM+OUAP4FUIfZJJrIVa8WFwxcsMmjlLTxp/I7+JfdvZjJSk3j1yYvbWFviyoSkpQgw2hIDhZxCg083Z6qS467g5H9Uz3fQc+Ss0K0Mud6RcZTU9RqCcp+h92tUc8+gDxQ2NwJsG5vcmSRwf5KHKvsWjt6yK4OHxCpkLYi31eJZtv2EjQGXX1gYyhc/2wQ+cHPvbgBzIfhXetbZKpSxoowAQO/J1i5oRs90h24kjTd4qJd3qspxk1lhZcEC8IkfZXjNgjEQI=");
//
//         OneDriveFolder folder = new OneDriveFolder(api, "PDF");
//
//         for (OneDriveItem.Metadata metadata : OneDriveFolder.getRoot(api)) {
//             System.out.println(metadata.getName());
//             if (metadata.isFile()) {
//             }
//         }
//
// //        for (OneDriveItem.Metadata search : OneDriveFolder.getRoot(api).search("index.html")) {
// //            System.out.println(search.getName());
// //        }
//
// //        OneDriveFile file = new OneDriveFile(api, "key.txt");
// //        System.out.println(file);
//     }
// }
